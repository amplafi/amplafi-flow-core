/*
 * Created on Apr 20, 2005
 */
package org.amplafi.flow.impl;

import static org.amplafi.flow.FlowConstants.*;
import static org.amplafi.flow.FlowLifecycleState.*;
import static org.amplafi.flow.FlowUtils.*;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNumeric;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.*;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;


/**
 * Application State Object that tracks the current state of a flow. Holds any
 * state information related to a specific flow
 *
 * Each flow state has all the information to run the flow and re-enter it if
 * needed.
 *
 * defines an actively executing flow. Each FlowState has an attached Flow which
 * is the instantiated definition. This copy is made to avoid problems with flow
 * definitions changing while an instance of a flow is active.
 */
public class FlowStateImpl implements FlowState {

    private static final long serialVersionUID = -7694935572121566257L;

    /**
     * used when displaying the FlowEntryPoint.
     */
    private String activeFlowLabel;

    /**
     * unique flow id so that flows can be started, stopped, restarted easily.
     */
    private String lookupKey;

    /**
     * key, value pairs that will be used to hold the current state of the flow.
     * The values here should be fairly lightweight.
     */
    protected FlowValuesMap flowValuesMap;

    private String flowTypeName;

    protected transient Flow flow;

    /**
     * index into flow.activities.
     */
    private Integer currentActivityIndex;

    /**
     * to be used when advancing flow to a fixed step. Use case: changing
     * flowtype.
     */
    private String currentActivityByName;

    /**
     * flowManagement instance that this FlowState is attached to.
     */
    private transient FlowManagement flowManagement;

    /**
     * a map of values that the current components have placed here for the
     * FlowActivity and Flow definitions to use for processing this request.
     * This map only contains values up until the completion of the
     * selectActivity() call
     */
    private transient Map<String, Object> cachedValues;

    private FlowLifecycleState flowLifecycleState;

    public FlowStateImpl() {
    }

    public FlowStateImpl(String flowTypeName, FlowManagement sessionFlowManagement,
            Map<String, String> initialFlowState) {
        this(flowTypeName, sessionFlowManagement);
        this.flowValuesMap = new DefaultFlowValuesMap(initialFlowState);
    }

    public FlowStateImpl(String flowTypeName, FlowManagement sessionFlowManagement) {
        this();
        this.flowLifecycleState = created;
        this.flowTypeName = flowTypeName;
        this.flowManagement = sessionFlowManagement;
        this.lookupKey = createLookupKey();
    }

    /**
     * @param instance
     * @param sessionFlowManagement
     */
    public FlowStateImpl(Flow instance, FlowManagement sessionFlowManagement) {
        this(instance.getFlowTypeName(), sessionFlowManagement);
        this.flow = instance;
        this.flow.setFlowState(this);
    }

    private String createLookupKey() {
        return this.flowTypeName + System.nanoTime();
    }

    @Override
    public String begin() {
        this.setFlowLifecycleState(initializing);
        initializeFlow();
        // TODO ... should we just be using next()... seems better.
        try {
            selectActivity(0, true);
        } finally {
            // because may throw flow validation exception
            if ( this.getFlowLifecycleState() == initializing) {
                this.setFlowLifecycleState(started);
            }
        }
        return getCurrentPage();
    }

    /**
     * @see org.amplafi.flow.FlowState#resume()
     */
    @Override
    public String resume() {
        if (!isActive()) {
            return begin();
        } else {
            if (getCurrentActivity().activate()) {
                selectActivity(nextIndex(), true);
            }
            return getCurrentPage();
        }
    }

    /**
     * Copy all Flow-level {@link org.amplafi.flow.FlowPropertyDefinition}'s initial values to the flowState's key value map.
     * Call each {@link FlowActivity#initializeFlow()}.
     */
    protected void initializeFlow() {
        Map<String, FlowPropertyDefinition> propertyDefinitions = this.getFlow().getPropertyDefinitions();
        if (propertyDefinitions != null) {
            Collection<FlowPropertyDefinition> flowPropertyDefinitions = propertyDefinitions.values();
            initializeFlow(flowPropertyDefinitions);
        }

        int size = this.getActivities().size();
        for (int i = 0; i < size; i++) {
            getActivity(i).initializeFlow();
        }
    }

    /**
     *
     * @see org.amplafi.flow.FlowState#morphFlow(java.lang.String, java.util.Map)
     */
    @Override
    public String morphFlow(String morphingToFlowTypeName, Map<String, String> initialFlowState) {
        if (isCompleted()) {
            return null;
        }
        if ( this.getFlowTypeName().equals(morphingToFlowTypeName)) {
            return this.getCurrentPage();
        }
        Flow nextFlow = getFlowManagement().getInstanceFromDefinition(morphingToFlowTypeName);
        List<FlowActivityImplementor> originalFAs = getActivities();
        List<FlowActivityImplementor> nextFAs = nextFlow.getActivities();

        // make sure FAs in both the flows are in order
        boolean inOrder = areFlowActivitiesInOrder(originalFAs, nextFAs);
        if (!inOrder) {
            throw new IllegalStateException("The FlowActivities in the original and the morphed flow are not in order"
                                            + "\nOriginal Flow FlowActivities : " + originalFAs
                                            + "\nNext Flow FlowActivities : " + nextFAs);
        }

        // complete the current FA in the current Flow
        FlowActivityImplementor currentFAInOriginalFlow = getCurrentFlowActivityImplementor();
        // So the current FlowActivity does not try to do validation.
        passivate(false);

        // morph and initialize to next flow
        setFlowTypeName(morphingToFlowTypeName);
        INSTANCE.copyMapToFlowState(this, initialFlowState);
        this.setCurrentActivityIndex(0);
        begin();

        FlowActivityImplementor targetFAInNextFlow = getTargetFAInNextFlow(currentFAInOriginalFlow,
                                                                originalFAs, nextFAs);

        // No common FAs, No need to run nextFlow at all, just return
        if (targetFAInNextFlow != null) {

            // move the second flow to appropriate FA
            while (hasNext() && !isEqualTo(getCurrentActivity(), targetFAInNextFlow)) {
                next();
            }
        }
        return this.getCurrentPage();
    }

    private FlowActivityImplementor getTargetFAInNextFlow(FlowActivityImplementor currentFAInOriginalFlow,
            List<FlowActivityImplementor> originalFAs, List<FlowActivityImplementor> nextFAs) {
        FlowActivity flowActivity = this.getActivity(currentFAInOriginalFlow.getActivityName());
        if ( flowActivity != null ) {
            // cool .. exact match on the names.
            return (FlowActivityImplementor) flowActivity;
        }
        // find the first FlowActivity that is after all the flowActivities with the same names
        // as FlowActivities in the previous flow.to find the same approximate spot in the the new flow.
        int newCurrentIndex = this.getCurrentActivityIndex();
        for (int prevIndex = 0; prevIndex < originalFAs.size(); prevIndex++) {
            FlowActivity originalFA = originalFAs.get(prevIndex);
            if ( isEqualTo(originalFA, currentFAInOriginalFlow)) {
                break;
            }
            for(int nextIndex = newCurrentIndex; nextIndex < nextFAs.size(); nextIndex++) {
                FlowActivity nextFA = nextFAs.get(nextIndex);
                if(isEqualTo(originalFA, nextFA)) {
                    newCurrentIndex = nextIndex+1;
                }
            }
        }
        return (FlowActivityImplementor) this.getActivity(newCurrentIndex);
    }

    private boolean isEqualTo(FlowActivity fa1, FlowActivity fa2) {
        return fa1 != null && fa2 != null && fa1.getActivityName().equals(fa2.getActivityName());
    }

    private boolean areFlowActivitiesInOrder(List<FlowActivityImplementor> prevFAs, List<FlowActivityImplementor> nextFAs) {
        int lastPrevIndex = -1;
        int lastNextIndex = -1;
        for(int prevIndex = 0; prevIndex < prevFAs.size(); prevIndex++) {
            for(int nextIndex = 0; nextIndex < nextFAs.size(); nextIndex++) {
                if ( isEqualTo(prevFAs.get(prevIndex), nextFAs.get(nextIndex))) {
                    if ( nextIndex > lastNextIndex && prevIndex > lastPrevIndex ) {
                        lastNextIndex = nextIndex;
                        lastPrevIndex = prevIndex;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    protected FlowState finishFlowActivities() {
        FlowState currentNextFlowState = getFlowManagement().transitionToFlowState(this);
        int size = this.getActivities().size();
        for (int i = 0; i < size; i++) {
            FlowActivity activity = getActivity(i);
            FlowState returned = activity.finishFlow(currentNextFlowState);
            // avoids lose track of FlowState if another FA is later in the Flow
            // definition.
            if (returned != null && currentNextFlowState != returned) {
                currentNextFlowState = returned;
            }
        }
        return currentNextFlowState;
    }

    /**
     * @see org.amplafi.flow.FlowState#initializeFlow(java.lang.Iterable)
     */
    @Override
    public void initializeFlow(Iterable<FlowPropertyDefinition> flowPropertyDefinitions) {
        for (FlowPropertyDefinition flowPropertyDefinition : flowPropertyDefinitions) {
            if (getRawProperty(flowPropertyDefinition.getName()) == null
                    && flowPropertyDefinition.getInitial() != null) {
                setProperty(flowPropertyDefinition.getName(), flowPropertyDefinition.getInitial());
            }
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#selectActivity(int, boolean)
     */
    @Override
    public FlowActivity selectActivity(int newActivity, boolean verifyValues) {
        if (isCompleted()) {
            return null;
        }
        // so selecting same activity is neither up nor down....
        int originalIndex = getCurrentActivityIndex();
        boolean goingForward = newActivity > originalIndex;
        boolean goingBack = newActivity < originalIndex;
        int next = newActivity;
        FlowActivity currentActivity;
        boolean lastBeginAutoFinished;
        boolean canContinue;
        do {
            if(this.isActive()) {
                FlowValidationResult flowValidationResult;
                // call passivate even if just returning to the current
                // activity. but not if we are going back to a previous step
                flowValidationResult = this.passivate(verifyValues);
                if ( !flowValidationResult.isValid()) {
                    getCurrentActivity().activate();
                    throw new FlowValidationException(getCurrentActivity(), flowValidationResult);
                }
            }
            this.setCurrentActivityIndex(next);

            currentActivity = getCurrentActivity();
            // TODO should really already be so...
            currentActivity.setActivatable(true);
            if (goingForward) {
                next = nextIndex();
                canContinue = hasNext();
            } else if (goingBack) {
                next = previousIndex();
                canContinue = hasPrevious();
            } else {
                canContinue = false;
            }
            lastBeginAutoFinished = currentActivity.activate();
        } while (lastBeginAutoFinished && canContinue);
        if (lastBeginAutoFinished && goingForward && !canContinue) {
            // ran out .. time to complete...
            // if chaining FlowStates the actual page may be from another
            // flowState.
            finishFlow();
            currentActivity = null;
        }
        return currentActivity;
    }

    /**
     * @see org.amplafi.flow.FlowState#selectVisibleActivity(int)
     */
    @Override
    public FlowActivity selectVisibleActivity(int visibleIndex) {
        int index = -1;
        int realIndex = -1;
        for (FlowActivity activity : getActivities()) {
            if (!activity.isInvisible()) {
                index++;
            }
            realIndex++;
            if (index == visibleIndex) {
                break;
            }
        }
        return selectActivity(realIndex, false);
    }

    /**
     * @see org.amplafi.flow.FlowState#saveChanges()
     */
    @Override
    public void saveChanges() {
        for (int i = 0; i < this.getActivities().size(); i++) {
            getActivity(i).saveChanges();
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#finishFlow()
     */
    @Override
    public String finishFlow() {
        return completeFlow(FlowLifecycleState.successful);
    }

    /**
     * @see org.amplafi.flow.FlowState#cancelFlow()
     */
    @Override
    public String cancelFlow() {
        return completeFlow(FlowLifecycleState.canceled);
    }

    protected String completeFlow(FlowLifecycleState nextFlowLifecycleState) {
        String pageName = null;
        if (!isCompleted()) {
            FlowState continueWithFlow = null;
            boolean verifyValues = nextFlowLifecycleState != FlowLifecycleState.canceled;
            FlowValidationResult flowValidationResult = passivate(verifyValues);

            if (verifyValues) {
                if (flowValidationResult.isValid()) {
                    saveChanges();
                } else {
                    throw new FlowValidationException(flowValidationResult);
                }
            }

            this.setFlowLifecycleState(nextFlowLifecycleState);
            boolean success = false;
            try {
                // getting continueWithFlow should use FlowLauncher more correctly.
                continueWithFlow = finishFlowActivities();
                success = true;
            } finally {
                this.setCurrentActivityByName(null);
                clearCache();
                if (!success) {
                    getFlowManagement().dropFlowState(this);
                }
            }
            // OLD note but may still be valid:
            // if continueWithFlow is not null then we do not want start
            // any other flows except continueWithFlow. Autorun flows should
            // start only if we have no flow specified by the finishingActivity. This
            // caused bad UI behavior when we used TransitionFlowActivity to start new
            // flow.
            // make sure that don't get into trouble by a finishFlow that
            // returns the current FlowState.
            if (continueWithFlow == null || continueWithFlow == this) {
                pageName = getFlowManagement().completeFlowState(this, false);
            } else {
                // pass on the return flow.
                String returnToFlow = this.getPropertyAsObject(FSRETURN_TO_FLOW);
                if ( isNotBlank(returnToFlow)) {
                    continueWithFlow.setProperty(FSRETURN_TO_FLOW, returnToFlow);
                }
                this.setProperty(FSRETURN_TO_FLOW, null);
                pageName = getFlowManagement().completeFlowState(this, true);
                if (!continueWithFlow.isActive()) {
                    pageName = continueWithFlow.begin();
                } else if (!continueWithFlow.isCompleted()) {
                    pageName = continueWithFlow.resume();
                }
                String continueWithFlowLookup;
                if (continueWithFlow.isCompleted()) {
                    continueWithFlowLookup = continueWithFlow.getPropertyAsObject(FSCONTINUE_WITH_FLOW);
                } else {
                    continueWithFlowLookup = continueWithFlow.getLookupKey();
                }
                // save back to "this" so that if the current flowState is in turn part of a chain that the callers
                // will find the correct continue flow state.
                setProperty(FSCONTINUE_WITH_FLOW, continueWithFlowLookup);
            }
        }
        // if afterPage is already set then don't lose that information.
        if (pageName != null) {
            setAfterPage(pageName);
        }
        return pageName;
    }

    /**
     * Clear the values from a flow state that should not be preserved after the
     * flow completes and is transitioning to another Flow.
     *
     * This reduces the size of the state and removes values that may conflict with the
     * new flow.
     */
    protected void clearProperties(FlowValuesMap valuesMap) {
        Map<String, FlowPropertyDefinition> propertyDefinitions = getFlow().getPropertyDefinitions();
        for(Map.Entry<String, FlowPropertyDefinition> flowPropertyDefinitionEntry: propertyDefinitions.entrySet()) {
            FlowPropertyDefinition value = flowPropertyDefinitionEntry.getValue();
            if ( value.getPropertyUsage().isClearOnExit()) {
                getLog().debug("clearing "+value);
                valuesMap.remove(flowPropertyDefinitionEntry.getKey());
            }
        }
    }

    /**
     * @param possibleReferencedState
     * @return true if this flowState references possibleReferencedState
     */
    public boolean isReferencing(FlowState possibleReferencedState) {
        if ( this == possibleReferencedState) {
            // can't reference self.
            return false;
        } else {
            return StringUtils.equals(this.getRawProperty(FSCONTINUE_WITH_FLOW),possibleReferencedState.getLookupKey())
                || StringUtils.equals(this.getRawProperty(FSRETURN_TO_FLOW), possibleReferencedState.getLookupKey());
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#getClearFlowValuesMap()
     */
    @Override
    public FlowValuesMap getClearFlowValuesMap() {
        DefaultFlowValuesMap defaultFlowValuesMap = new DefaultFlowValuesMap(getFlowValuesMap());
        clearProperties(defaultFlowValuesMap);
        return defaultFlowValuesMap;
    }

    @Override
    public FlowValidationResult passivate(boolean verifyValues) {
        FlowActivity currentActivity = getCurrentActivity();
        if ( currentActivity != null ) {
            currentActivity.refresh();
            return currentActivity.passivate(verifyValues);
        }
        return null;
    }
    /**
     * @see org.amplafi.flow.FlowState#getCurrentPage()
     */
    @Override
    public String getCurrentPage() {
        if (isCompleted()) {
            return this.getAfterPage();
        }
        String pageName = getPropertyAsObject(FSPAGE_NAME);
        if (isBlank(pageName)) {
            if (isActive()) {
                FlowActivity flowActivity = getCurrentActivity();
                pageName = flowActivity.getPageName();
            }
            if (isBlank(pageName)) {
                pageName = this.getFlow().getPageName();
            }
        }
        return pageName;
    }

    /**
     * @see org.amplafi.flow.FlowState#setActiveFlowLabel(java.lang.String)
     */
    @Override
    public void setActiveFlowLabel(String activeFlowLabel) {
        this.activeFlowLabel = activeFlowLabel;
    }

    /**
     * @see org.amplafi.flow.FlowState#getActiveFlowLabel()
     */
    @Override
    public String getActiveFlowLabel() {
        if (this.activeFlowLabel != null) {
            return activeFlowLabel;
        } else {
            return getFlow().getContinueFlowTitle();
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#getActivity(int)
     */
    @Override
    public <T extends FlowActivity> T getActivity(int activityIndex) {
        T flowActivity = (T) this.getFlow().getActivity(activityIndex);
        return resolveActivity(flowActivity);
    }

    /**
     * All accesses to a {@link FlowActivity} should occur through this method.
     * This allows {@link FlowState} implementations to a chance to add in any
     * objects needed to access other parts of the service (database transactions for example).
     * @see FlowManagement#resolveFlowActivity(FlowActivity)
     * @param flowActivity
     * @return flowActivity
     */
    public <T extends FlowActivity> T resolveActivity(T flowActivity) {
        getFlowManagement().resolveFlowActivity(flowActivity);
        return flowActivity;
    }

    /**
     * @see org.amplafi.flow.FlowState#getActivity(java.lang.String)
     */
    @Override
    public <T extends FlowActivity> T  getActivity(String activityName) {
        // HACK we need to set up a map.
        for (FlowActivity flowActivity : this.getFlow().getActivities()) {
            if (flowActivity.getActivityName().equals(activityName)) {
                return (T) resolveActivity(flowActivity);
            }
        }
        return null;
    }

    /**
     * @see org.amplafi.flow.FlowState#getLookupKey()
     */
    @Override
    public String getLookupKey() {
        return lookupKey;
    }

    @Override
    public boolean hasLookupKey(Object key) {
        if (key == null) {
            return false;
        } else {
            return getLookupKey().equals(key.toString());
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#getCurrentActivity()
     */
    @Override
    public FlowActivity getCurrentActivity() {
        return getActivity(this.getCurrentActivityIndex());
    }
    public FlowActivityImplementor getCurrentFlowActivityImplementor() {
        return (FlowActivityImplementor) getActivity(this.getCurrentActivityIndex());
    }
    /**
     * Use selectActivity to change the current activity.
     *
     * @param currentActivity The currentActivity to set.
     */
    private void setCurrentActivityIndex(int currentActivity) {
        if (currentActivity >= 0 && currentActivity < size()) {
            this.currentActivityIndex = currentActivity;
            this.currentActivityByName = getCurrentActivity().getActivityName();
        } else {
            // required to match the iterator definition.
            throw new NoSuchElementException(currentActivity + ": incorrect index for "
                                             + this.activeFlowLabel);
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#setCurrentActivityByName(java.lang.String)
     */
    @Override
    public void setCurrentActivityByName(String currentActivityByName) {
        this.currentActivityByName = currentActivityByName;
        this.currentActivityIndex = null;
    }

    /**
     * @see org.amplafi.flow.FlowState#getCurrentActivityByName()
     */
    @Override
    public String getCurrentActivityByName() {
        return this.currentActivityByName;
    }

    /**
     * @see org.amplafi.flow.FlowState#size()
     */
    @Override
    public int size() {
        return getFlow().getActivities().size();
    }

    /**
     * @see org.amplafi.flow.FlowState#getCurrentActivityIndex()
     */
    @Override
    public int getCurrentActivityIndex() {
        if (currentActivityIndex == null) {
            currentActivityIndex = -1;
            if (isNotBlank(currentActivityByName)) {
                int i = 0;
                for (FlowActivity flowActivity : this.getFlow().getActivities()) {
                    if (currentActivityByName.equals(flowActivity.getActivityName())) {
                        currentActivityIndex = i;
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
        return currentActivityIndex;
    }

    /**
     * @see org.amplafi.flow.FlowState#getRawProperty(java.lang.String)
     */
    @Override
    public String getRawProperty(String key) {
        return ObjectUtils.toString(getFlowValuesMap().get(key), null);
    }

    /**
     * @see org.amplafi.flow.FlowState#getProperty(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String getProperty(String flowActivityName, String key) {
        return ObjectUtils.toString(getFlowValuesMap().get(flowActivityName, key), null);
    }

    /**
     * @see org.amplafi.flow.FlowState#getProperty(org.amplafi.flow.FlowActivity,
     *      org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition) {
        T result = (T) getCached(propertyDefinition.getName());
        if ( result == null ) {
            String value = getRawProperty(flowActivity, propertyDefinition.getName());
            result = (T) propertyDefinition.parse(value);
            if (result == null && propertyDefinition.isAutoCreate()) {
                result =  (T) propertyDefinition.getDefaultObject(flowActivity);
            }
            setCached(propertyDefinition.getName(), result);
        }
        return result;
    }

    @Override
    public <T> void setProperty(FlowActivity flowActivity, FlowPropertyDefinition propertyDefinition, T value) {
        Object actual;
        String stringValue = null;
        if (value instanceof String && propertyDefinition.getDataClass() != String.class) {
            // handle case for when initializing from string values.
            // or some other raw format.
            stringValue = (String) value;
            actual = propertyDefinition.parse(stringValue);
        } else {
            actual = value;
        }
        boolean cacheValue = !(actual instanceof String);
        if (!propertyDefinition.isCacheOnly()) {
            cacheValue &= this.setRawProperty(flowActivity, propertyDefinition.getName(),
                                              stringValue == null ? propertyDefinition.serialize(actual) : stringValue);
        }
        if (cacheValue) {
            // HACK FPD can't currently parse AmpEntites to actual objects.
            this.setCached(propertyDefinition.getName(), actual);
        }

    }

    /**
     * @see org.amplafi.flow.FlowState#setProperty(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean setProperty(String flowActivityName, String key, String value) {
        String oldValue = getProperty(flowActivityName, key);
        if (!equalsIgnoreCase(value, oldValue)) {
            boolean b = Boolean.parseBoolean(getProperty(flowActivityName, FSREADONLY));
            if (b) {
                throw new IllegalStateException(flowActivityName+"."+key+": no change allowed to readonly flow properties");
            }
            FlowActivityImplementor activity = getActivity(flowActivityName);
            value = activity.propertyChange(flowActivityName, key, value,
                                                                 oldValue);
            getFlowValuesMap().put(flowActivityName, key, value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#setRawProperty(org.amplafi.flow.FlowActivity,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean setRawProperty(FlowActivity flowActivity, String key, String value) {
        String oldValue = getProperty(flowActivity.getActivityName(), key);
        if (oldValue == null) {
            return setProperty(key, value);
        } else {
            return setProperty(flowActivity.getActivityName(), key, value);
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#setProperty(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean setProperty(String key, String value) {
        String oldValue = getRawProperty(key);
        if (!equalsIgnoreCase(value, oldValue)) {
            Boolean b = getBoolean(FSREADONLY);
            if (b != null && b) {
                throw new IllegalStateException(key+": no change allowed to readonly flow properties");
            }
            if (this.isActive()) {
                value = getCurrentFlowActivityImplementor().propertyChange(null, key, value, oldValue);
                if (value == oldValue) {
                    return false;
                }
            }
            getFlowValuesMap().put(key, value);
            // in other way wrong cached value returns in next get request
            setCached(key, null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#hasProperty(java.lang.String)
     */
    @Override
    public boolean hasProperty(String key) {
        return getFlowValuesMap().containsKey(key);
    }

    /**
     * @see org.amplafi.flow.FlowState#getActivities()
     */
    @Override
    public List<FlowActivityImplementor> getActivities() {
        return getFlow().getActivities();
    }

    /**
     * @see org.amplafi.flow.FlowState#getVisibleActivities()
     */
    @Override
    public List<FlowActivity> getVisibleActivities() {
        return getFlow().getVisibleActivities();
    }

    /**
     * @see org.amplafi.flow.FlowState#isFinishable()
     */
    @Override
    public boolean isFinishable() {
        FlowActivity currentActivity = this.getCurrentActivity();
        if (currentActivity.isFinishingActivity()) {
            return true;
        } else {
            List<FlowActivity> visible = getVisibleActivities();
            return visible.get(visible.size() - 1) == currentActivity;
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#clearCache()
     */
    @Override
    public void clearCache() {
        if (this.cachedValues != null) {
            this.cachedValues.clear();
            this.cachedValues = null;
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#getFlowTitle()
     */
    @Override
    public String getFlowTitle() {
        String flowTitle = getPropertyAsObject(FlowConstants.FSTITLE_TEXT);
        if (isBlank(flowTitle)) {
            flowTitle = this.getFlow().getFlowTitle();
        }
        if (isBlank(flowTitle)) {
            flowTitle = this.getFlow().getLinkTitle();
        }
        return flowTitle;
    }

    /**
     * @see org.amplafi.flow.FlowState#setCached(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public synchronized void setCached(String key, Object value) {
        if (cachedValues == null) {
            if ( value == null) {
                // nothing to cache and no cached values.
                return;
            }
            cachedValues = new HashMap<String, Object>();
            flowManagement.registerForCacheClearing();
        }
        if ( value == null ) {
            cachedValues.remove(key);
        } else {
            cachedValues.put(key, value);
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#getCached(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCached(String key) {
        return (T) (cachedValues != null? cachedValues.get(key) : null);
    }

    /**
     * @see org.amplafi.flow.FlowState#setFlowManagement(org.amplafi.flow.FlowManagement)
     */
    @Override
    public void setFlowManagement(FlowManagement flowManagement) {
        this.flowManagement = flowManagement;
    }

    /**
     * @see org.amplafi.flow.FlowState#getFlowManagement()
     */
    @Override
    public FlowManagement getFlowManagement() {
        return flowManagement;
    }

    /**
     * @see org.amplafi.flow.FlowState#setFlowTypeName(java.lang.String)
     */
    @Override
    public synchronized void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
        this.flow = null;
    }

    /**
     * @see org.amplafi.flow.FlowState#getFlowTypeName()
     */
    @Override
    public String getFlowTypeName() {
        return flowTypeName;
    }

    /**
     * @see org.amplafi.flow.FlowState#getFlow()
     */
    @Override
    public synchronized Flow getFlow() {
        if (this.flow == null) {
            this.flow = this.flowManagement.getInstanceFromDefinition(getFlowTypeName());
            if (this.flow == null) {
                throw new IllegalArgumentException(getFlowTypeName() + ": no such flow definition");
            }
            this.flow.setFlowState(this);
        }
        return flow;
    }

    /**
     * @see org.amplafi.flow.FlowState#iterator()
     */
    @Override
    public Iterator<FlowActivity> iterator() {
        return this;
    }

    /**
     * @see org.amplafi.flow.FlowState#next()
     */
    @Override
    public FlowActivity next() {
        if (hasNext()) {
            return this.selectActivity(nextIndex(), true);
        } else {
            finishFlow();
            return null;
        }

    }

    /**
     * @see org.amplafi.flow.FlowState#previous()
     */
    @Override
    public FlowActivity previous() {
        return this.selectActivity(previousIndex(), false);
    }

    /**
     * @see org.amplafi.flow.FlowState#makeCurrent()
     */
    @Override
    public String makeCurrent() {
        return this.flowManagement.makeCurrent(this);
    }

    @Override
    @SuppressWarnings("unused")
    public void add(FlowActivity e) {
        throw new UnsupportedOperationException("TODO: Auto generated");
    }

    /**
     * @see org.amplafi.flow.FlowState#hasVisibleNext()
     */
    @Override
    public boolean hasVisibleNext() {
        if (!hasNext()) {
            return false;
        }
        int count = getActivities().size();
        for (int i = getCurrentActivityIndex() + 1; i < count; i++) {
            if (!getActivity(i).isInvisible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.amplafi.flow.FlowState#hasVisiblePrevious()
     */
    @Override
    public boolean hasVisiblePrevious() {
        if (!hasPrevious()) {
            return false;
        }
        for (int i = getCurrentActivityIndex() - 1; i >= 0; i--) {
            if (!getActivity(i).isInvisible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.amplafi.flow.FlowState#hasNext()
     */
    @Override
    public boolean hasNext() {
        if (isCompleted()) {
            return false;
        } else {
//        } else if ( getCurrentActivity().getFlowValidationResult().isValid()) {
            int count = getActivities().size();
            return this.getCurrentActivityIndex() < count - 1;
//        } else {
//            // TODO -- this seems bad because hasNext() seems like it should be constant.
//            return false;
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#hasPrevious()
     */
    @Override
    public boolean hasPrevious() {
        if (isCompleted()) {
            return false;
        } else {
            return this.getCurrentActivityIndex() > 0;
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#nextIndex()
     */
    @Override
    public int nextIndex() {
        return this.getCurrentActivityIndex() + 1;
    }

    /**
     * @see org.amplafi.flow.FlowState#previousIndex()
     */
    @Override
    public int previousIndex() {
        return this.getCurrentActivityIndex() - 1;
    }

    /**
     * @see org.amplafi.flow.FlowState#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("TODO: Auto generated");
    }

    @Override
    @SuppressWarnings("unused")
    public void set(FlowActivity e) {
        throw new UnsupportedOperationException("TODO: Auto generated");
    }

    /**
     * @see org.amplafi.flow.FlowState#isTrue(java.lang.String)
     */
    @Override
    public boolean isTrue(String key) {
        Boolean b = getBoolean(key);
        return b != null && b;
    }

    /**
     * @see org.amplafi.flow.FlowState#getBoolean(java.lang.String)
     */
    @Override
    public Boolean getBoolean(String key) {
        String value = getRawProperty(key);
        if (value == null) {
            return null;
        } else {
            return Boolean.valueOf(value);
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#getLong(java.lang.String)
     */
    @Override
    public Long getLong(String key) {
        String value = getRawProperty(key);
        if (value == null) {
            return null;
        } else if (isNumeric(value)) {
            return Long.parseLong(value);
        } else {
            throw new IllegalArgumentException(this.activeFlowLabel + ": not numeric value for"
                                               + key + "=" + value);
        }
    }

    @Override
    public String toString() {
        return this.lookupKey + " [type:" + this.flowTypeName + "]";
    }

    /**
     * @see org.amplafi.flow.FlowState#getCurrentActivityFlowValidationResult()
     */
    @Override
    public FlowValidationResult getCurrentActivityFlowValidationResult() {
        FlowActivity currentActivity = this.getCurrentActivity();
        return currentActivity == null ? null : currentActivity.getFlowValidationResult();
    }

    /**
     * @see org.amplafi.flow.FlowState#isCurrentActivityCompletable()
     */
    @Override
    public boolean isCurrentActivityCompletable() {
        return getCurrentActivityFlowValidationResult().isValid();
    }

    /**
     * @see org.amplafi.flow.FlowState#getFlowValidationResults()
     */
    @Override
    public Map<String, FlowValidationResult> getFlowValidationResults() {
        Map<String, FlowValidationResult> result = new LinkedHashMap<String, FlowValidationResult>();
        for (FlowActivity activity : this.getActivities()) {
            FlowValidationResult flowValidationResult = activity.getFlowValidationResult();
            if (!flowValidationResult.isValid()) {
                result.put(activity.getActivityName(), flowValidationResult);
            }
        }
        return result;
    }

    /**
     * @see org.amplafi.flow.FlowState#setAfterPage(java.lang.String)
     */
    @Override
    public void setAfterPage(String afterPage) {
        this.setProperty(FSAFTER_PAGE, afterPage);
    }

    /**
     * @see org.amplafi.flow.FlowState#getAfterPage()
     */
    @Override
    public String getAfterPage() {
        // if (this.flowLifecycleState == canceled) {
        // return null;
        // }
        String page = getPropertyAsObject(FSAFTER_PAGE, String.class);
        if (isNotBlank(page)) {
            return page;
        }
        page = getPropertyAsObject(FSDEFAULT_AFTER_PAGE, String.class);
        if (isNotBlank(page)) {
            return page;
        } else {
            return flow == null ? null : flow.getDefaultAfterPage();
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#isUpdatePossible()
     */
    @Override
    public boolean isUpdatePossible() {
        return StringUtils.isNotBlank(getUpdateText());
    }

    /**
     * @see org.amplafi.flow.FlowState#getUpdateText()
     */
    @Override
    public String getUpdateText() {
        return this.getPropertyAsObject(FAUPDATE_TEXT, String.class);
    }

    /**
     * @see org.amplafi.flow.FlowState#isCancelPossible()
     */
    @Override
    public boolean isCancelPossible() {
        Boolean b = this.getBoolean(FSNO_CANCEL);
        return b == null || !b;
    }

    /**
     * @see org.amplafi.flow.FlowState#getCancelText()
     */
    @Override
    public String getCancelText() {
        return this.getPropertyAsObject(FSCANCEL_TEXT, String.class);
    }

    /**
     * @see org.amplafi.flow.FlowState#setCancelText(java.lang.String)
     */
    @Override
    public void setCancelText(String cancelText) {
        this.setProperty(FSCANCEL_TEXT, cancelText);
    }

    /**
     * @see org.amplafi.flow.FlowState#getFinishText()
     */
    @Override
    public String getFinishText() {
        return this.getPropertyAsObject(FSFINISH_TEXT, String.class);
    }

    /**
     * @see org.amplafi.flow.FlowState#setFinishText(java.lang.String)
     */
    @Override
    public void setFinishText(String finishText) {
        this.setProperty(FSFINISH_TEXT, finishText);
    }

    /**
     * @see org.amplafi.flow.FlowState#setFinishType(java.lang.String)
     */
    @Override
    public void setFinishType(String type) {
        this.setProperty(FSALT_FINISHED, type);
    }

    /**
     * @see org.amplafi.flow.FlowState#getFinishType()
     */
    @Override
    public String getFinishType() {
        return this.getPropertyAsObject(FSALT_FINISHED, String.class);
    }

    /**
     * @see org.amplafi.flow.FlowState#setFlowLifecycleState(org.amplafi.flow.FlowLifecycleState)
     */
    @Override
    public void setFlowLifecycleState(FlowLifecycleState flowLifecycleState) {
        checkAllowed(this.flowLifecycleState, flowLifecycleState);
        this.flowLifecycleState = flowLifecycleState;
    }

    /**
     * @see org.amplafi.flow.FlowState#getFlowLifecycleState()
     */
    @Override
    public FlowLifecycleState getFlowLifecycleState() {
        return this.flowLifecycleState;
    }

    /**
     * @see org.amplafi.flow.FlowState#isCompleted()
     */
    @Override
    public boolean isCompleted() {
        return this.flowLifecycleState != null
        && this.flowLifecycleState != started && this.flowLifecycleState != initializing;
    }

    @Override
    public String getPropertyAsObject(String key) {
        return getPropertyAsObject(key, String.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPropertyAsObject(String key, Class<T> expected) {
        if (isActive()) {
            return (T) getCurrentActivity().getProperty(key);
        } else {
            FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinition(key);

            if (flowPropertyDefinition == null) {
                flowPropertyDefinition = createFlowPropertyDefinition(key, expected, null);
            }
            return (T) getProperty(null, flowPropertyDefinition);
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#isActive()
     */
    @Override
    public boolean isActive() {
        // TODO | HACK Seems like we should be looking at FlowLifecycleState here not the index range.
        return this.getCurrentActivityIndex() >= 0 && getCurrentActivityIndex() < size();
    }

    @Override
    public <T> FlowPropertyDefinition getFlowPropertyDefinition(String key) {
        FlowPropertyDefinition flowPropertyDefinition = null;
        if ( this.getFlow() != null ) {
            flowPropertyDefinition = this.getFlow().getPropertyDefinition(key);
        }
        if (flowPropertyDefinition == null) {
            flowPropertyDefinition = this.getFlowManagement().getFlowPropertyDefinition(key);
        }
        return flowPropertyDefinition;
    }

    /**
     * @see org.amplafi.flow.FlowState#setPropertyAsObject(java.lang.String,
     *      java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void setPropertyAsObject(String key, T value) {
        if (isActive()) {
            getCurrentActivity().setProperty(key, value);
        } else {
            Class<T> expected = (Class<T>) (value == null?null:value.getClass());
            FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinition(key);

            if (flowPropertyDefinition == null) {
                flowPropertyDefinition = createFlowPropertyDefinition(key, expected, value);
            }
            setProperty(null, flowPropertyDefinition, value);
        }
    }

    /**
     * @see org.amplafi.flow.FlowState#setDefaultAfterPage(java.lang.String)
     */
    @Override
    public void setDefaultAfterPage(String pageName) {
        this.setProperty(FSDEFAULT_AFTER_PAGE, pageName);
    }

    /**
     * @see org.amplafi.flow.FlowState#getDefaultAfterPage()
     */
    @Override
    public String getDefaultAfterPage() {
        String property = this.getPropertyAsObject(FSDEFAULT_AFTER_PAGE);
        return property == null ? this.getFlow().getDefaultAfterPage() : property;
    }

    /**
     * @see org.amplafi.flow.FlowState#isNotCurrentAllowed()
     */
    @Override
    public boolean isNotCurrentAllowed() {
        return this.getFlow().isNotCurrentAllowed();
    }

    /**
     * @see org.amplafi.flow.FlowState#getFlowValuesMap()
     */
    @Override
    public FlowValuesMap getFlowValuesMap() {
        if (this.flowValuesMap == null) {
            this.flowValuesMap = new DefaultFlowValuesMap();
        }
        return this.flowValuesMap;
    }

    /**
     * @see org.amplafi.flow.FlowState#setFlowValuesMap(org.amplafi.flow.FlowValuesMap)
     */
    @Override
    public void setFlowValuesMap(FlowValuesMap flowValuesMap) {
        this.flowValuesMap = flowValuesMap;
    }

    /**
     * @see org.amplafi.flow.FlowState#getLog()
     */
    @Override
    public Log getLog() {
        return getFlowManagement().getLog();
    }

    /**
     * @see org.amplafi.flow.FlowState#getRawProperty(org.amplafi.flow.FlowActivity,
     *      java.lang.String)
     */
    @Override
    public String getRawProperty(FlowActivity flowActivity, String key) {
        String value = flowActivity == null ? null : getProperty(flowActivity.getActivityName(),
                                                                 key);
        if (value == null) {
            value = getRawProperty(key);
        }
        return value;

    }

    /**
     * Get an object from the database.  If the object does not exist in the database a value of null will be returned.
     *
     * This uses a Hibernate get() call rather than a load() call to prevent us from getting errors if an entity can't be found
     * by the ID passed in.  This exception gets thrown whenever an object field is first accessed which can be well above the data
     * access code.
     * @param <T>
     * @param <K>
     *
     * @param clazz The class of the object to load
     * @param entityId The id of the object to load
     * @return The loaded object or null if it doesn't exist
     */
    public <T, K> T load(Class<? extends T> clazz, K entityId) {
        // We need to use get() to load entities as opposed to load() in case we try to get an entity via an id that doesn't pair
        // to a record in the database.  If we can figure out a way to validate the record actually exists then we can
        // switch this to use load() and not incur the up-front overhead.
        return getFlowManagement().getFlowTx().get(clazz, entityId, true);
    }

    /**
     * @see org.amplafi.flow.FlowState#getRawLong(org.amplafi.flow.FlowActivity,
     *      java.lang.String)
     */
    @Override
    public Long getRawLong(FlowActivity flowActivity, String key) {
        String value = getRawProperty(flowActivity, key);
        if (isNotEmpty(value) && isNumeric(value)) {
            return Long.parseLong(value);
        } else {
            return null;
        }

    }

    @Override
    public <T> FlowPropertyDefinition createFlowPropertyDefinition(String key,
            Class<T> expected, T sampleValue) {
        FlowPropertyDefinition propertyDefinition;
        if (expected != null && !CharSequence.class.isAssignableFrom(expected)) {
            // auto define property
            // TODO save the definition when the flowState is persisted.
            // From Sasha (25-Mar-2008): added Hibernate.getClass to have always
            // real class in propertyDefinition, not proxy,
            // because if we have proxy as dataClass we can't assign real class
            // from it.
            // propertyDefinition = new FlowPropertyDefinition(key,
            // Hibernate.getClass(sampleValue));
            propertyDefinition = new FlowPropertyDefinitionImpl(key, expected);
            getFlow().addPropertyDefinition(propertyDefinition);
        } else {
            // assume it to be the default type. but don't save it
            propertyDefinition = new FlowPropertyDefinitionImpl(key);
        }
        return propertyDefinition;
    }

}
