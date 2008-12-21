/*
 * Created on May 9, 2005
 */
package org.amplafi.flow;

import static org.amplafi.flow.FlowConstants.*;
import static org.amplafi.flow.flowproperty.PropertyUsage.*;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNumeric;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.amplafi.flow.flowproperty.ChainedFlowPropertyValueProvider;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyValueProvider;
import org.amplafi.flow.flowproperty.PropertyRequired;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.FlowValidationResult;
import org.amplafi.flow.validation.InconsistencyTracking;
import org.amplafi.flow.validation.MissingRequiredTracking;
import org.amplafi.flow.validation.ReportAllValidationResult;
import org.amplafi.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * defines one activity in the flow. This can be a definition of an activity or
 * the actual activity depending on the state of the Flow parent object.
 * FlowActivity objects may be part of multiple definitions or multiple
 * instances (but not both instances and definitions).
 *
 * FlowActivities must be stateless. FlowActivity instances can be reused
 * between different users and different {@link Flow}s.
 *
 * <p/> Lifecycle methods:
 * <ol>
 * <li>{@link #initializeFlow()} - used to initialize the FlowState with any
 * defaults for missing values. <b>No</b> modifications should occur in this
 * method.</li>
 * <li>{@link #activate()} - called each time the FlowActivity is made the
 * current FlowActivity. Returns true if the Flow should immediately advance to
 * the next FlowActivity. If this is the last FlowActivity, then the Flow
 * completes. <b>No</b> modifications should occur in this method.</li>
 * <li>{@link #passivate(boolean)} - called each time the FlowActivity was the
 * current FlowActivity and is now no longer the current FlowActivity. Used to
 * validate input as needed. <b>No</b> modifications should occur in this
 * method.</li>
 * <li>{@link #saveChanges()} - called when the flow is completing. <i>Only
 * place where db modifications can be made.</i> This allows canceling the flow
 * to meaningfully revert all changes.</li>
 * <li>{@link #finishFlow(FlowState)} - called when the flow is finishing.</li>
 * </ol>
 * <p/> This structure is in place so that FlowActivities that create
 * relationships are not put into the position of having to be aware of the
 * surrounding Flow and previously created objects. Nor are they aware of the
 * state of the flow. <p/> By convention, FlowActivies are expected to be in a
 * 'flows' package and the FlowActivity subclass' name ends with 'FlowActivity'.
 * <p/> If a FlowActivity is a visible step then the FlowActivity needs a
 * component. The default component type is the grandparent package + the
 * FlowActivity class name with 'FlowActivity' stripped off. For example,
 * fuzzy.flows.FooBarFlowActivity would have a default component of
 * 'fuzzy/FooBar'. <p/> TODO handle return to previous flow issues.
 */
public class FlowActivity implements Serializable {

    private static final long serialVersionUID = 5578715117421910908L;

    /**
     * The page name that this FlowActivity will activate.
     */
    private String pageName;

    /**
     * The component name that this FlowActivity will activate.
     */
    private String componentName;

    /**
     * This is the activity name (id) of this FlowActivity.
     */
    private String activityName;

    /**
     * The flow title that the appears in the flow picture.
     */
    private String activityTitle;

    /**
     * means that while there may be more possible steps, those steps are
     * optional and the user may exit gracefully out of the flow if this
     * activity is the current activity.
     */
    private boolean finishingActivity;

    /**
     * indicates that this flow activity is accessible. Generally, each previous
     * step must be completed for the activity to be available.
     */
    private boolean activatable;

    private boolean invisible;

    private boolean persistFlow;

    /**
     * if this is an instance, this is the {@link Flow} instance.
     */
    private Flow flow;

    /**
     * if this is an instance {@link FlowActivity}, then this is the definition
     * {@link FlowActivity}.
     */
    private FlowActivity definitionFlowActivity;

    /**
     * null if this is an instance.
     */
    private Map<String, FlowPropertyDefinition> propertyDefinitions;

    private static final Pattern compNamePattern = Pattern
            .compile("([\\w]+)\\.flows\\.([\\w]+)FlowActivity$");

    public FlowActivity() {
        if (this.getClass() != FlowActivity.class) {
            // set default activity name
            activityName = this.getClass().getSimpleName();

            String name = this.getClass().getName();
            Matcher m = compNamePattern.matcher(name);
            if (m.find()) {
                componentName = m.group(1) + "/" + m.group(2);
            }
        }
    }

    /**
     * subclasses should override to add their standard definitions.
     */
    protected void addStandardFlowPropertyDefinitions() {
        // see #2179 #2192
        this.addPropertyDefinitions(
            new FlowPropertyDefinition(FATITLE_TEXT).initPropertyUsage(activityLocal),
            new FlowPropertyDefinition(FAUPDATE_TEXT).initPropertyUsage(activityLocal),
            new FlowPropertyDefinition(FANEXT_TEXT).initPropertyUsage(activityLocal),
            new FlowPropertyDefinition(FAPREV_TEXT).initPropertyUsage(activityLocal)
        );
    }

    /**
     * called when a flow is started. Subclasses should override this method to
     * initialize the FlowState with default values if needed. <p/> The
     * FlowState's initialState has been set. So all properties should be
     * checked first to see if they have already been set. <p/> No database
     * modifications should occur in this method.
     */
    public void initializeFlow() {
        Map<String, FlowPropertyDefinition> props = this.getPropertyDefinitions();
        if (props != null) {
            for (FlowPropertyDefinition propertyDefinition : props.values()) {
                // move values from alternateNames to the true name.
                // or just clear out the alternate names of their values.
                for (String alternateName : propertyDefinition.getAlternates()) {
                    String altProperty = getRawProperty(alternateName);
                    setRawProperty(alternateName, null);
                    if (isNotBlank(altProperty)) {
                        initPropertyIfNull(propertyDefinition.getName(), altProperty);
                    }
                }
                // if the property is not allowed to be overridden then we force
                // initialize it.
                String initial = propertyDefinition.getInitial();
                if (initial != null) {
                    if (!propertyDefinition.isInitialMode()) {
                        String currentValue = getRawProperty(propertyDefinition.getName());
                        if (!StringUtils.equals(initial, currentValue)) {
                            if (currentValue != null) {
                                throw new IllegalArgumentException(
                                        this.getFullActivityName()
                                                + '.'
                                                + propertyDefinition.getName()
                                                + " cannot be set to '"
                                                + currentValue
                                                + "' external to the flow. It is being force to the initial value of '"
                                                + initial + "'");
                            }
                            setProperty(propertyDefinition.getName(), initial);
                        }
                    } else {
                        initPropertyIfNull(propertyDefinition.getName(), initial);
                    }
                }
            }
        }
    }

    /**
     * subclass should override this to perform some action when the
     * FlowActivity is activated. But should still call this method.
     *
     * this method checks to see if the flowActivity is invisible or if
     * {@link FlowConstants#FSAUTO_COMPLETE} = true and
     * {@link #getFlowValidationResult()} {@link FlowValidationResult#isValid()}
     * = true.
     *
     * @return if true, immediately complete this FlowActivity.
     */
    public boolean activate() {
        // Check for missing required parameters
        FlowValidationResult activationValidationResult = getFlowValidationResult(PropertyRequired.activate);
        if (!activationValidationResult.isValid()) {
            throw new FlowValidationException(activationValidationResult);
        }

        if (!invisible) {
            boolean autoComplete = isTrue(FSAUTO_COMPLETE);
            if (autoComplete) {
                FlowValidationResult flowValidationResult = getFlowValidationResult();
                return flowValidationResult.isValid();
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Passivate this flow activity. This method is invoked whenever the flow
     * activity is submitted (i.e. next, previous or finish is clicked on the
     * flow). Override this method to create the objects, or update objects as
     * needed when leaving this flow activity. TODO: I don't see this getting
     * called on PREVIOUS, shouldn't it?
     *
     * @param verifyValues verify the current values that the FlowActivity is
     *        concerned about.
     * @return the {@link FlowValidationResult} if verifyValues is true otherwise an empty {@link FlowValidationResult} object.
     */
    public FlowValidationResult passivate(boolean verifyValues) {
        if (verifyValues) {
            FlowValidationResult validationResult = getFlowValidationResult();
            return validationResult;
        }
        return new ReportAllValidationResult();
    }

    /**
     * Some properties do not have simple representation. for those objects
     * their internal state may have changed. This give opportunity for that
     * internal state change to be saved into flowState.
     */
    protected void saveBack() {
        Map<String, FlowPropertyDefinition> definitions = this.getPropertyDefinitions();
        if ( MapUtils.isNotEmpty(definitions)) {
            for (Map.Entry<String, FlowPropertyDefinition> entry : definitions.entrySet()) {
                if (!entry.getValue().isCacheOnly() && entry.getValue().isSaveBack()) {
                    Object cached = getCached(entry.getKey());
                    if (cached != null) {
                        // this means that we miss case when the value has been
                        // discarded.
                        // but otherwise much worse case when the value simply was
                        // not accessed
                        // results in the property being cleared.
                        setProperty(entry.getKey(), cached);
                    }
                }
            }
        }
    }
    /**
     * called when changes accumulated to flow properties should be saved
     * permanently. Override this method to perform database updates.
     */
    public void saveChanges() {

    }

    /**
     * called when the FlowActivity is marked as a Flow's finishingActivity.
     * <p/> called after all FlowActivities' {@link #saveChanges()} have been
     * called.
     * @param currentNextFlowState TODO
     *
     * @return the next FlowState that is now the current FlowState.
     */
    public FlowState finishFlow(FlowState currentNextFlowState) {
        return currentNextFlowState;
    }

    public Flow getFlow() {
        return flow;
    }

    /**
     * @param pageName The pageName to set.
     */
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    /**
     * @return Returns the pageName.
     */
    public String getPageName() {
        return pageName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * creates a new Flow instance. The flow definition of the new Flow is
     * spawnFlowTypeName. This is an optional flow from this current flow.
     *
     * @param spawnFlowTypeName the flow definition to use to create the new
     *        flow instance.
     * @return created {@link FlowState}
     */
    protected FlowState createNewFlow(String spawnFlowTypeName) {
        FlowManagement fm = getFlowManagement();
        FlowState createdFlowState = fm.createFlowState(spawnFlowTypeName, getFlowState().getClearFlowValuesMap(), false);
        return createdFlowState;
    }

    /**
     * can the current FlowActivity's completeActivity() method be called.
     *
     * @return result of validation
     */
    public FlowValidationResult getFlowValidationResult() {
        return this.getFlowValidationResult(PropertyRequired.advance);
    }

    /**
     * Determines if the flow passes validation for a specific level of required properties.
     * @param required
     *
     * @return result of validation
     */
    public FlowValidationResult getFlowValidationResult(PropertyRequired required) {
        FlowValidationResult result = new ReportAllValidationResult();
        Map<String, FlowPropertyDefinition> propDefs = getPropertyDefinitions();
        if (MapUtils.isNotEmpty(propDefs)) {
            for (FlowPropertyDefinition def : propDefs.values()) {
                if ((required != null && def.getPropertyRequired() == required)
                        && isPropertyNotSet(def.getName())
                        && def.getDefaultObject(this) == null && def.getFlowPropertyValueProvider() == null) {
                    result.addTracking(new MissingRequiredTracking(def.getParameterName()));
                }
            }
        }
        return result;
    }

    /**
     * Helps describing 'missing value' problems.
     *
     * @param result keeps track of validation results
     * @param value if true then the property is *NOT set correctly and we need
     *        a {@link MissingRequiredTracking}.
     * @param property missing property's name
     */
    protected void appendRequiredTrackingIfTrue(FlowValidationResult result, boolean value,
            String property) {
        if (value) {
            result.addTracking(new MissingRequiredTracking(property));
        }
    }

    /**
     * Helps describing 'incorrect value' problems.
     *
     * @param result keeps track of validation results
     * @param value if true then we need to inform of an inconsistency (using
     *        {@link InconsistencyTracking}) described be the key and data
     *        parameters.
     * @param key The key that describes the inconsistency.
     * @param data Additional values to use for generating the message that
     *        describes the problem.
     */
    protected void appendInconsistencyTrackingIfTrue(FlowValidationResult result, boolean value,
            String key, String... data) {
        if (value) {
            result.addTracking(new InconsistencyTracking(key, data));
        }
    }

    /**
     * @param activityName The activityName to set.
     */
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    /**
     * @return Returns the activityName.
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * @param activityTitle The flowTitle to set.
     */
    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    /**
     * @return Returns the flowTitle.
     */
    public String getActivityTitle() {
        String title = getString(FATITLE_TEXT);
        if (isNotBlank(title)) {
            return title;
        } else if (isNotBlank(activityTitle)){
            return activityTitle;
        } else {
            return "message:"+ "flow.activity." + FlowUtils.INSTANCE.toLowerCase(this.getActivityName() ) +".title";
        }
    }

    /**
     * "flowName.activityName"
     *
     * @return full flow activity name.
     */
    public String getFullActivityName() {
        return this.getFlow().getFlowTypeName() + "." + getActivityName();
    }

    /**
     * @param activatable true if this flowActivity can be selected from the UI.
     */
    public void setActivatable(boolean activatable) {
        this.activatable = activatable;
    }

    /**
     * Used to control which FlowActivities a user may select. This is used to
     * prevent the user from jumping ahead in a flow.
     *
     * @return true if this FlowActivity can be activated by the user.
     */
    public boolean isActivatable() {
        return activatable;
    }

    /**
     * @param finishedActivity The user can finish the flow when this activity
     *        is current.
     */
    public void setFinishingActivity(boolean finishedActivity) {
        finishingActivity = finishedActivity;
    }

    /**
     * @return true if the user can finish the flow when this activity is
     *         current.
     */
    public boolean isFinishingActivity() {
        return finishingActivity;
    }

    public FlowManagement getFlowManagement() {
        return this.getFlowState() == null ? null : this.getFlowState().getFlowManagement();
    }

    public FlowState getFlowState() {
        return flow == null? null:flow.getFlowState();
    }

    @SuppressWarnings("unchecked")
    public <T> T dup() {
        FlowActivity clone;
        try {
            clone = this.getClass().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        copyTo(clone);
        return (T) clone;
    }

    public FlowActivity createInstance() {
        FlowActivity instance = dup();
        copyTo(instance);
        instance.definitionFlowActivity = this;
        return instance;
    }

    protected void copyTo(FlowActivity instance) {
        instance.activatable = activatable;
        instance.activityName = activityName;
        instance.activityTitle = activityTitle;
        instance.componentName = componentName;
        instance.pageName = pageName;
        instance.finishingActivity = finishingActivity;
        instance.invisible = invisible;
        instance.persistFlow = persistFlow;
    }

    public void setPropertyDefinitions(Map<String, FlowPropertyDefinition> properties) {
        propertyDefinitions = properties;
    }

    public Map<String, FlowPropertyDefinition> getPropertyDefinitions() {
        if (propertyDefinitions == null && isInstance()) {
            // as is usually the case for instance flow activities.
            return definitionFlowActivity.getPropertyDefinitions();
        }
        return propertyDefinitions;
    }

    public boolean isInstance() {
        return definitionFlowActivity != null;
    }

    public FlowActivity initInvisible() {
        setInvisible(true);
        return this;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setPersistFlow(boolean persistFlow) {
        this.persistFlow = persistFlow;
    }

    public boolean isPersistFlow() {
        return persistFlow;
    }

    public <T> FlowPropertyDefinition getPropertyDefinition(String key) {
        FlowPropertyDefinition propertyDefinition = getLocalPropertyDefinition(key);
        if (propertyDefinition == null) {
            propertyDefinition = getFlowPropertyDefinition(key);
        }
        return propertyDefinition;
    }

    private FlowPropertyDefinition getFlowPropertyDefinition(String key) {
        FlowPropertyDefinition flowPropertyDefinition = null;
        if ( this.getFlowState() != null) {
            flowPropertyDefinition = getFlowState().getFlowPropertyDefinition(key);
        }
        // should be else if
        if ( flowPropertyDefinition == null && this.getFlow() != null) {
            flowPropertyDefinition = this.getFlow().getPropertyDefinition(key);
        }
        return flowPropertyDefinition;
    }

    private FlowPropertyDefinition getLocalPropertyDefinition(String key) {
        Map<String, FlowPropertyDefinition> propDefs = this.getPropertyDefinitions();
        FlowPropertyDefinition def = null;
        if (propDefs != null) {
            def = propDefs.get(key);
        }
        return def;
    }

    public void addPropertyDefinition(FlowPropertyDefinition definition) {
        FlowPropertyDefinition currentLocal;
        if (propertyDefinitions == null) {
            propertyDefinitions = new LinkedHashMap<String, FlowPropertyDefinition>();
            currentLocal = null;
        } else {
            currentLocal = getLocalPropertyDefinition(definition.getName());
        }
        // check against the FlowPropertyDefinition
        if (!definition.merge(currentLocal)) {
            getLog().warn(this.getFlow().getFlowTypeName()
                                    + "."
                                    + this.getActivityName()
                                    + " has a FlowPropertyDefinition '"
                                    + definition.getName()
                                    + "' that conflicts with previous definition. Previous definition discarded.");
            propertyDefinitions.remove(definition.getName());
        }
        if (!definition.isLocal()) {
            // this property may be from the Flow definition itself.
            FlowPropertyDefinition current = this.getFlowPropertyDefinition(definition.getName());
            if (!definition.merge(current)) {
                getLog().warn(this.getFlow().getFlowTypeName()
                                        + "."
                                        + this.getActivityName()
                                        + " has a FlowPropertyDefinition '"
                                        + definition.getName()
                                        + "' that conflicts with flow's definition. The FlowActivity's definition will be marked as local only.");
                definition.setPropertyUsage(activityLocal);
            } else {
                pushPropertyDefinitionToFlow(definition);
            }
        }
        propertyDefinitions.put(definition.getName(), definition);
    }

    protected void pushPropertyDefinitionToFlow(FlowPropertyDefinition definition) {
        if (getFlow() != null && !definition.isLocal()) {
            FlowPropertyDefinition flowProp = this.getFlow().getPropertyDefinition(
                    definition.getName());
            if (flowProp == null ) {
                // push up to flow so that other can see it.
                FlowPropertyDefinition cloned = definition.clone();
                // a FPD may be pushed so for an earlier FA may not require the property be set.
                cloned.setRequired(false);
                this.getFlow().addPropertyDefinition(cloned);
            } else if ( flowProp.isMergeable(definition)) {
                flowProp.merge(definition);
                flowProp.setRequired(false);
            }
        }
    }

    protected void pushPropertyDefinitionsToFlow() {
        if (MapUtils.isNotEmpty(propertyDefinitions) && getFlow() != null) {
            for (FlowPropertyDefinition definition : propertyDefinitions.values()) {
                pushPropertyDefinitionToFlow(definition);
            }
        }
    }

    public void addPropertyDefinitions(FlowPropertyDefinition... definitions) {
        for (FlowPropertyDefinition definition : definitions) {
            this.addPropertyDefinition(definition);
        }
    }

    public void addPropertyDefinitions(Iterable<FlowPropertyDefinition> definitions) {
        for (FlowPropertyDefinition def : definitions) {
            addPropertyDefinition(def);
        }
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public int getIndex() {
        return flow.indexOf(this);
    }

    public boolean isPropertyNotSet(String key) {
        return getRawProperty(key) == null;
    }

    public boolean isPropertySet(String key) {
        return getRawProperty(key) != null;
    }

    public boolean isPropertyNotBlank(String key) {
        String v = getRawProperty(key);
        return isNotBlank(v);
    }

    public boolean isPropertyBlank(String key) {
        String v = getRawProperty(key);
        return isBlank(v);
    }

    public boolean isPropertyNumeric(String key) {
        String v = getRawProperty(key);
        return isNotEmpty(v) && isNumeric(v);
    }

    /**
     * Return the raw string value representation of the value indexed by key.
     * This should be used only rarely, as it bypasses all of the normal
     * property processing code. <p/> Permissible uses include cases are rare
     * and does not include retrieving a String property.
     *
     * @param key
     * @return raw string property.
     */
    public String getRawProperty(String key) {
        return getFlowState().getRawProperty(this, key);
    }

    /**
     * set a value with key specified in either the flowActivity specific values
     * (if such a flowactivity specific value is set already) or the global flow
     * values otherwise.
     *
     * @param key
     * @param value
     * @return {@link FlowState#setRawProperty(FlowActivity, String, String)}
     */
    public boolean setRawProperty(String key, String value) {
        return getFlowState().setRawProperty(this, key, value);
    }

    public Boolean getRawBoolean(String key) {
        String value = getRawProperty(key);
        return Boolean.parseBoolean(value);
    }

    public Long getRawLong(String key) {
        return getFlowState().getRawLong(this, key);
    }

    public void setRawLong(String key, Long value) {
        this.setRawProperty(key, value != null ? value.toString() : null);
    }

    // TODO .. injected
    protected FlowTx getTx() {
        return getFlowManagement().getFlowTx();
    }

    public void delete(Object entity) {
        getTx().delete(entity);
    }

    public <T> boolean flushIfNeeded(T... entities) {
        return getTx().flushIfNeeded(entities);
    }

    /**
     * override to treat some properties as special. This method is called by
     * FlowPropertyBinding.
     *
     * @param key
     * @param <T> type of property.
     * @return property
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        T result = (T) getCached(key);
        if (result == null) {
            FlowPropertyDefinition flowPropertyDefinition = getPropertyDefinition(key);
            if (flowPropertyDefinition == null) {
                flowPropertyDefinition = getFlowState().createFlowPropertyDefinition(key, null, null);
            }
            result = (T) getFlowState().getProperty(this, flowPropertyDefinition);
        }
        return result;
    }

    /**
     * Convert dataClass to a string using
     * {@link FlowUtils#toPropertyName(Class)} and use that string to look up
     * the property.
     *
     * @param <T> type of property
     * @param dataClass type of property
     * @return the value converted to dataClass.
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(Class<T> dataClass) {
        return (T) getProperty(FlowUtils.INSTANCE.toPropertyName(dataClass));
    }

    public String getString(String key) {
        return getProperty(key);
    }

    public Boolean getBoolean(String key) {
        return getProperty(key);
    }

    public boolean isTrue(String key) {
        Boolean b = getBoolean(key);
        return b != null && b;
    }

    public boolean isFalse(String key) {
        return !isTrue(key);
    }

    /**
     * override to treat some properties as special. This method is called by
     * FlowPropertyBinding. Default behavior caches value and sets the property
     * to value.
     *
     * @param key
     * @param value
     * @param <T> value's type
     */
    public <T> void setProperty(String key, T value) {
        Class<T> expected = (Class<T>) (value == null?null:value.getClass());
        FlowPropertyDefinition propertyDefinition = getPropertyDefinition(key);
        if (propertyDefinition == null && getFlowState() != null) {
            propertyDefinition = getFlowState().createFlowPropertyDefinition(key, expected, value);
        }
        getFlowState().setProperty(this, propertyDefinition, value);
    }

    /**
     * Convert value.getClass() to string using
     * {@link FlowUtils#toPropertyName(Class)} and use that string as the
     * property name for the value being set.
     *
     * @param <T>
     * @param value must not be null
     */
    public <T> void setProperty(T value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        } else {
            setProperty(value.getClass(), value);
        }
    }

    /**
     * Convert value.getClass() to string using
     * {@link FlowUtils#toPropertyName(Class)} and use that string as the
     * property name for the value being set.
     *
     * @param <T> value's type
     * @param value may be null
     * @param dataClass
     */
    public <T> void setProperty(Class<? extends T> dataClass, T value) {
        setProperty(FlowUtils.INSTANCE.toPropertyName(dataClass), value);
    }

    /**
     * Only called if value != oldValue.
     *
     * @param flowActivityName
     * @param key
     * @param value
     * @param oldValue
     * @return what the value should be. Usually just return the value
     *         parameter.
     */
    @SuppressWarnings("unused")
    protected String propertyChange(String flowActivityName, String key, String value,
            String oldValue) {
        return value;
    }

    /**
     * save an object in the cache. This cache is flushed when the current
     * transaction has been committed. This flushing is necessary because
     * otherwise there will be errors with accessing data outside a transaction.
     *
     * @param <T>
     * @param key
     * @param value
     */
    protected <T> T cache(String key, T value) {
        getFlowState().setCached(key, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getCached(String key) {
        FlowState flowState = getFlowState();
        return flowState == null ? null : (T) flowState.getCached(key);
    }

    @Override
    public String toString() {
        Flow f = getFlow();
        if ( f != null ) {
            return f.getFlowTypeName()+"."+activityName+ " " +getClass()+" id="+super.toString();
        } else {
            return activityName+ " " +getClass()+" id="+super.toString();
        }
    }

    /**
     * If the property has no value stored in the flowState's keyvalueMap then
     * put the supplied value in it.
     *
     * @param key
     * @param value
     * @see #isPropertyNotSet(String)
     */
    public void initPropertyIfNull(String key, Object value) {
        if (isPropertyNotSet(key)) {
            this.setProperty(key, value);
        }
    }

    public void initPropertyIfBlank(String key, Object value) {
        if (isPropertyBlank(key)) {
            this.setProperty(key, value);
        }
    }

    // TODO inject....
    protected Log getLog() {
        FlowManagement flowManagement = getFlowManagement();
        return flowManagement != null ? flowManagement.getLog() : LogFactory
                .getLog(this.getClass());
    }

    /**
     * @param value may start with {@link FlowConstants#FLOW_PROPERTY_PREFIX}
     * @return value if does not start with
     *         {@link FlowConstants#FLOW_PROPERTY_PREFIX}, otherwise look up the
     *         property value referenced and return that value.
     */
    protected String resolve(String value) {
        if (value != null && value.startsWith(FLOW_PROPERTY_PREFIX)) {
            return getString(value.substring(FLOW_PROPERTY_PREFIX.length()));
        } else {
            return value;
        }
    }

    protected String getResolvedProperty(String key) {
        return resolve(getString(key));
    }

    // TODO refactor to FlowState ?
    protected JSONObject getRawJsonObject(String key) {
        String rawProperty = getRawProperty(key);
        return JSONObject.toJsonObject(rawProperty);
    }

    /**
     * Called before a flow is {@link #passivate(boolean)} also called if the flow is not
     * advancing but wants to inform the current FlowActivity that all the
     * values from the UI have been refreshed so the FlowActivity can do any
     * in-place updates. This can be used for the rare case when it is desireable
     * to immediately commit a change to the database.
     */
    public void refresh() {
        saveBack();
    }

    protected void redirectTo(String uri) {
        setProperty(FlowConstants.FSREDIRECT_URL, uri);
    }

    /**
     * HACK ... should only be called from  {@link #addStandardFlowPropertyDefinitions()} NOT {@link #initializeFlow()}
     * this is because adding to standard properties will not happen correctly ( the {@link org.amplafi.flow.translator.FlowTranslatorResolver} is
     * visible.
     * other wise will affect the definitions.
     * see #2179 / #2192
     */
    protected void handleFlowPropertyValueProvider(String key, FlowPropertyValueProvider flowPropertyValueProvider) {
        FlowPropertyDefinition flowPropertyDefinition = this.getLocalPropertyDefinition(key);
        if ( flowPropertyDefinition != null) {
            if ( flowPropertyValueProvider instanceof ChainedFlowPropertyValueProvider) {
                ((ChainedFlowPropertyValueProvider)flowPropertyValueProvider).setPrevious(flowPropertyDefinition.getFlowPropertyValueProvider());
            }
            flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
        }
        flowPropertyDefinition = this.getFlowPropertyDefinition(key);
        if ( flowPropertyDefinition != null) {
            if ( flowPropertyValueProvider instanceof ChainedFlowPropertyValueProvider) {
                ((ChainedFlowPropertyValueProvider)flowPropertyValueProvider).setPrevious(flowPropertyDefinition.getFlowPropertyValueProvider());
            }
            flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
        }
    }

    /**
     *
     */
    public void processDefinitions() {
        addStandardFlowPropertyDefinitions();
        pushPropertyDefinitionsToFlow();
    }
}