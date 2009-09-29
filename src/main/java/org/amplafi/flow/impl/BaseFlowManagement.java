/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.amplafi.flow.impl;

import static org.amplafi.flow.FlowConstants.*;
import static org.apache.commons.lang.StringUtils.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowManager;
import org.amplafi.flow.FlowLifecycleState;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowTransition;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.FlowTx;
import org.amplafi.flow.PropertyUsage;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.flowproperty.PropertyScope;
import org.amplafi.flow.web.PageProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A basic implementation of FlowManagement.
 *
 *
 */
public class BaseFlowManagement implements FlowManagement {

    private static final long serialVersionUID = -6759548552816525625L;

    protected SessionFlows sessionFlows = new SessionFlows();

    private FlowManager flowManager;
    private FlowTx flowTx;

    private PageProvider pageProvider;

    private FlowTranslatorResolver flowTranslatorResolver;

    /**
     * @see org.amplafi.flow.FlowManagement#getFlowStates()
     */
    public List<FlowState> getFlowStates() {
        List<FlowState> collection= new ArrayList<FlowState>();
        CollectionUtils.addAll(collection, sessionFlows.iterator());
        return collection;
    }

    /**
     * @see org.amplafi.flow.FlowManagement#dropFlowState(org.amplafi.flow.FlowState)
     */
    public String dropFlowState(FlowState flow) {
        return this.dropFlowStateByLookupKey(flow.getLookupKey());
    }

    public String getCurrentPage() {
        FlowState flow = getCurrentFlowState();
        if (flow != null ) {
            return flow.getCurrentPage();
        } else {
            return null;
        }
    }

    /**
     * @see org.amplafi.flow.FlowManagement#getCurrentFlowState()
     */
    @SuppressWarnings("unchecked")
    public synchronized <FS extends FlowState> FS getCurrentFlowState() {
        if (sessionFlows.isEmpty()) {
            return null;
        }
        return (FS) sessionFlows.getFirst();
    }
    /**
     * @see org.amplafi.flow.FlowManagement#getCurrentActivity()
     */
    public FlowActivity getCurrentActivity() {
        FlowState current = getCurrentFlowState();
        if ( current != null ) {
            return current.getCurrentActivity();
        } else {
            return null;
        }
    }

    /**
     * @see org.amplafi.flow.FlowManagement#getActiveFlowStatesByType(java.lang.String...)
     */
    public synchronized List<FlowState> getActiveFlowStatesByType(String... flowTypes) {
        List<String> types=Arrays.asList(flowTypes);
        ArrayList<FlowState> result = new ArrayList<FlowState>();
        for (FlowState flowState: sessionFlows) {
            if (types.contains(flowState.getFlowTypeName())) {
                result.add(flowState);
            }
        }
        return result;
    }

    /**
     * @see org.amplafi.flow.FlowManagement#getFirstFlowStateByType(java.lang.String...)
     */
    @SuppressWarnings("unchecked")
    public synchronized <FS extends FlowState> FS getFirstFlowStateByType(String... flowTypes) {
        if(flowTypes == null){
            return null;
        }
        List<String> types=Arrays.asList(flowTypes);
        return (FS) getFirstFlowStateByType(types);
    }

    @SuppressWarnings("unchecked")
    public <FS extends FlowState> FS getFirstFlowStateByType(Collection<String> types) {
        for (FlowState flowState: sessionFlows) {
            if (types.contains(flowState.getFlowTypeName())) {
                return (FS)flowState;
            }
        }
        return null;
    }
    public Flow getFlowDefinition(String flowTypeName) {
        return getFlowManager().getFlowDefinition(flowTypeName);
    }

    /**
     * @see org.amplafi.flow.FlowManagement#getFlowState(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public <FS extends FlowState> FS getFlowState(String lookupKey) {
        FS flowState;
        if ( isNotBlank(lookupKey)) {
            flowState = (FS) sessionFlows.get(lookupKey);
        } else {
            flowState = null;
        }
        return flowState;
    }
    /**
     * Override this method to create a custom {@link FlowState} object.
     * @param flowTypeName
     * @param initialFlowState
     * @return the newly created FlowsState
     */
    @SuppressWarnings("unchecked")
    protected <FS extends FlowState> FS makeFlowState(String flowTypeName, Map<String, String> initialFlowState) {
        return (FS) new FlowStateImpl(flowTypeName, this, initialFlowState);
    }
    /**
     * @see org.amplafi.flow.FlowManagement#createFlowState(java.lang.String, java.util.Map, boolean)
     * TODO : sees like method should be renamed.
     */
    @SuppressWarnings("unchecked")
    public synchronized <FS extends FlowState> FS createFlowState(String flowTypeName, Map<String, String> initialFlowState, boolean makeNewStateCurrent) {
        FS flowState = (FS) makeFlowState(flowTypeName, initialFlowState);
        initializeFlowState(flowState);
        if (makeNewStateCurrent || this.sessionFlows.isEmpty()) {
            makeCurrent(flowState);
        } else {
            makeLast(flowState);
        }
        return flowState;
    }
    @SuppressWarnings({ "unused", "unchecked" })
    public <FS extends FlowState> FS createFlowState(String flowTypeName, FlowState initialFlowState, Map<String, String> initialValues, boolean makeNewStateCurrent) {
        FS flowState = (FS) createFlowState(flowTypeName, initialFlowState.getFlowValuesMap(), makeNewStateCurrent);
        return flowState;
    }

    protected <FS extends FlowState> void initializeFlowState(FS flowState) {
        flowState.initializeFlow();
    }


    /**
     * @see org.amplafi.flow.FlowManagement#transitionToFlowState(FlowState, String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public FlowState transitionToFlowState(FlowState flowState, String key) {
        FlowState nextFlowState = null;
        Map<String, FlowTransition> transitions = flowState.getPropertyAsObject(key, Map.class);
        String finishKey = flowState.getFinishType();
        if ( MapUtils.isNotEmpty(transitions) && isNotBlank(finishKey)) {
            FlowTransition flowTransition = transitions.get(finishKey);
            if ( flowTransition != null ) {
                FlowActivityImplementor currentActivity = flowState.getCurrentActivity();
                String flowType = currentActivity.resolve(flowTransition.getNextFlowType());
                if (isNotBlank(flowType)) {
                    nextFlowState = this.createFlowState(flowType, flowState.getExportedValuesMap(), false);
                }
            }
        }
        return nextFlowState;
    }

    /**
     * @see org.amplafi.flow.FlowManagement#startFlowState(java.lang.String, boolean, java.util.Map, Object)
     */
    @SuppressWarnings("unchecked")
    public <FS extends FlowState> FS startFlowState(String flowTypeName, boolean makeNewStateCurrent, Map<String, String> initialFlowState, Object returnToFlow) {
        initialFlowState = initReturnToFlow(initialFlowState, returnToFlow);
        FS flowState = (FS) createFlowState(flowTypeName, initialFlowState, makeNewStateCurrent);
        /* If you want tapestry stuff injected here...
        // set default page to go to after flow if flow is successful
        if (flowState.getDefaultAfterPage() == null ) {
            IPage currentCyclePage;
            try {
                currentCyclePage = cycle.getPage();
            } catch(NullPointerException e) {
                // because of the way cycle is injected - it is impossible to see if the cycle is null.
                // (normal java checks are looking at the proxy object)
                currentCyclePage = null;
            }
            if ( currentCyclePage != null) {
                flowState.setDefaultAfterPage(currentCyclePage.getPageName());
            }
        }
        */
        return (FS) beginFlowState(flowState);
    }

    /**
     * @param initialFlowState
     * @param returnToFlow
     * @return initialFlowState if existed otherwise a new map if returnToFlow was legal.
     */
    protected Map<String, String> initReturnToFlow(Map<String, String> initialFlowState, Object returnToFlow) {
        if ( returnToFlow != null) {
            String returnToFlowLookupKey = null;
            if ( returnToFlow instanceof Boolean) {
                if ( ((Boolean)returnToFlow).booleanValue()) {
                    FlowState currentFlowState = getCurrentFlowState();
                    if ( currentFlowState != null) {
                        returnToFlowLookupKey = currentFlowState.getLookupKey();
                    }
                }
            } else if ( returnToFlow instanceof FlowState) {
                returnToFlowLookupKey = ((FlowState)returnToFlow).getLookupKey();
            } else {
                returnToFlowLookupKey = returnToFlow.toString();
            }
            if ( isNotBlank(returnToFlowLookupKey)) {
                if ( initialFlowState == null) {
                    initialFlowState = new HashMap<String, String>();
                }
                initialFlowState.put(FSRETURN_TO_FLOW, returnToFlowLookupKey);
            }
        }
        return initialFlowState;
    }
    /**
     * @see org.amplafi.flow.FlowManagement#startFlowState(java.lang.String, boolean, java.lang.Object, java.lang.Iterable, Object)
     */
    @SuppressWarnings("unchecked")
    public <FS extends FlowState> FS startFlowState(String flowTypeName, boolean makeNewStateCurrent, Object propertyRoot, Iterable<String> initialValues, Object returnToFlow) {
        Map<String, String> initialMap = convertToMap(propertyRoot, initialValues);
        return (FS) startFlowState(flowTypeName, makeNewStateCurrent, initialMap, returnToFlow);
    }
    /**
     * @see org.amplafi.flow.FlowManagement#continueFlowState(java.lang.String, boolean, java.lang.Object, java.lang.Iterable)
     */
    @SuppressWarnings("unchecked")
    public <FS extends FlowState> FS continueFlowState(String lookupKey, boolean makeStateCurrent, Object propertyRoot, Iterable<String> initialValues) {
        Map<String, String> initialMap = convertToMap(propertyRoot, initialValues);
        return (FS) continueFlowState(lookupKey, makeStateCurrent, initialMap);
    }

    private Map<String, String> convertToMap(Object propertyRoot, Iterable<String> initialValues) {
        Map<String, String> initialMap = new HashMap<String, String>();
        if ( initialValues != null) {

            for(String entry: initialValues) {
                String[] v = entry.split("=");
                String key = v[0];
                String lookup;
                if ( v.length < 2 ) {
                    lookup = key;
                } else {
                    lookup = v[1];
                }
                Object value = getValueFromBinding(propertyRoot, lookup);
                initialMap.put(key, value == null?null:value.toString());
            }
        }
        return initialMap;
    }
    /**
     * @see org.amplafi.flow.FlowManagement#continueFlowState(java.lang.String, boolean, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public <FS extends FlowState> FS continueFlowState(String lookupKey, boolean makeStateCurrent, Map<String, String> initialFlowState) {
        FS flowState = (FS) getFlowState(lookupKey);
        if ( flowState == null) {
            throw new IllegalArgumentException(lookupKey+": no flow with this lookupKey found");
        }
        if (MapUtils.isNotEmpty(initialFlowState)) {
            for(Map.Entry<String, String> entry: initialFlowState.entrySet()) {
                // HACK this looks bad. At the very least shouldn't FlowUtils.copyState be used
                // more likely PropertyUsage/PropertyScope
                ((FlowStateImplementor)flowState).setRawProperty(entry.getKey(), entry.getValue());
            }
        }
        if (makeStateCurrent) {
            makeCurrent(flowState);
        }
        return flowState;
    }
    /**
     * Used to map object properties into flowState values.
     * <p/>
     * TODO: current implementation ignores lookup and just returns the root object.
     *
     * @param root the root object.
     * @param lookup the property 'path' that will result in the value to be assigned.
     * For example, 'bar.foo' will look for a 'bar' property in the "current" object.
     * That 'bar' object should have a 'foo' property. The 'foo' property will be returned
     * as the value to be stored into the FlowState map.
     * @return the value that root and lookup bind to.
     */
    @SuppressWarnings("unused")
    protected Object getValueFromBinding(Object root, String lookup) {
        return root;
    }
    /**
     * call flowState's {@link FlowState#begin()}. If flowState is now completed then see if the flow has transitioned to a new flow.
     *
     * @param flowState
     * @return flowState if flowState has not completed, otherwise the continue flow or the return flow.
     */
    @SuppressWarnings("unchecked")
    protected <FS extends FlowState> FS beginFlowState(FlowState flowState) {
        boolean success = false;
        try {
            flowState.begin();
            success = true;
            if ( flowState.isCompleted()) {
                FS state = (FS) getNextFlowState(flowState);
                if ( state != null) {
                    return state;
                }
            }
            return (FS) flowState;
        } finally {
            if ( !success ) {
                this.dropFlowState(flowState);
            }
        }
    }

    /**
     * @param flowState
     * @return
     */
    @SuppressWarnings("unchecked")
    private <FS extends FlowState> FS getNextFlowState(FlowState flowState) {
        String id = flowState.getPropertyAsObject(FSCONTINUE_WITH_FLOW);
        FS next = null;
        if ( isNotBlank(id)) {
            next = (FS) this.getFlowState(id);
        }
        if ( next == null ) {
            id = flowState.getPropertyAsObject(FSRETURN_TO_FLOW);
            if ( isNotBlank(id)) {
                next = (FS) this.getFlowState(id);
            }
        }
        return next;
    }

    /**
     * @see org.amplafi.flow.FlowManagement#resolveFlowActivity(org.amplafi.flow.FlowActivity)
     */
    @Override
    public void resolveFlowActivity(FlowActivity activity) {
        getFlowTranslatorResolver().resolve(activity);
    }
    /**
     * @see org.amplafi.flow.FlowManagement#dropFlowStateByLookupKey(java.lang.String)
     */
    public synchronized String dropFlowStateByLookupKey(String lookupKey) {
        if ( !sessionFlows.isEmpty()) {
            FlowStateImplementor fs = sessionFlows.getFirst();
            boolean first = fs.hasLookupKey(lookupKey);
            fs = (FlowStateImplementor) sessionFlows.removeByLookupKey(lookupKey);
            if ( fs != null ) {
                if ( !fs.getFlowLifecycleState().isTerminatorState()) {
                    fs.setFlowLifecycleState(FlowLifecycleState.canceled);
                }

                // look for redirect before clearing the flow state
                // why before cache clearing?
                String redirect = fs.getPropertyAsObject(FSREDIRECT_URL);
                String returnToFlowId = fs.getPropertyAsObject(FSRETURN_TO_FLOW);
                FlowState returnToFlow = getFlowState(returnToFlowId);
                fs.clearCache();

                if ( !first ) {
                    // dropped flow was not the current flow
                    // so we return the current flow's page.
                    return sessionFlows.getFirst().getCurrentPage();
                } else if (redirect!=null) {
                    return redirect;
                } else if ( returnToFlow != null) {
                    return makeCurrent(returnToFlow);
                } else if ( returnToFlowId != null ) {
                    getLog().warn("FlowState ("+fs.getLookupKey()+ ") trying to return to a flowState ("+returnToFlowId+") that could not be found.");
                }
                if ( !sessionFlows.isEmpty() ) {
                    return makeCurrent(sessionFlows.getFirst());
                } else {
                    // no other flows...
                    return fs.getAfterPage();
                }
            }
        }
        return null;
    }

    /**
     * Called by the {@link FlowState#finishFlow()}
     * @param newFlowActive pass false if you already have flow to run.
     */
    @Override
    @SuppressWarnings("unused")
    public String completeFlowState(FlowState flowState, boolean newFlowActive) {
        return dropFlowState(flowState);
    }
    /**
     * @see org.amplafi.flow.FlowManagement#makeCurrent(org.amplafi.flow.FlowState)
     */
    @Override
    public synchronized String makeCurrent(FlowState state) {
        if ( !this.sessionFlows.isEmpty()) {
            FlowStateImplementor oldFirst = this.sessionFlows.getFirst();
            if ( oldFirst == state) {
                // state is already the first state.
                return state.getCurrentPage();
            } else {
                sessionFlows.remove((FlowStateImplementor)state);
                if ( !oldFirst.isNotCurrentAllowed()) {
                    // the formerly first state is only supposed to be active if it is the first state.
                    // see if it this state is referenced as a return state -- otherwise the oldFirst will need to be dropped.
                    boolean notReferenced = state.isReferencing(oldFirst);
                    if ( !notReferenced ) {
                        for (FlowState flowState: this.sessionFlows) {
                            if( flowState.isReferencing(oldFirst)) {
                                notReferenced = false;
                                break;
                            }
                        }
                    }
                    if ( !notReferenced ) {
                        dropFlowState(oldFirst);
                    }
                } else {
                    oldFirst.clearCache();
                }
            }
        }
        this.sessionFlows.makeFirst((FlowStateImplementor)state);
        return state.getCurrentPage();
    }

    protected void makeLast(FlowState flowState) {
        this.sessionFlows.addLast((FlowStateImplementor)flowState);
    }
    /**
     * @see org.amplafi.flow.FlowManagement#makeAfter(org.amplafi.flow.FlowState, org.amplafi.flow.FlowState)
     */
    @Override
    public boolean makeAfter(FlowState flowState, FlowState nextFlowState) {
        boolean wasFirst = this.sessionFlows.makeAfter((FlowStateImplementor)flowState, (FlowStateImplementor)nextFlowState) == 0;
        if ( wasFirst) {
            makeCurrent(this.sessionFlows.getFirst());
        }
        return wasFirst;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> FlowPropertyDefinition createFlowPropertyDefinition(FlowPropertyProvider flowPropertyProvider, String key, Class<T> expected, T sampleValue) {
        if ( expected == null && sampleValue != null) {
            expected = (Class<T>) sampleValue.getClass();
        }
        FlowPropertyDefinitionImpl propertyDefinition = new FlowPropertyDefinitionImpl(key).initAccess(PropertyScope.global, PropertyUsage.io);
        if (expected != null && !CharSequence.class.isAssignableFrom(expected) ) {
            // auto define property
            // TODO save the definition when the flowState is persisted.
            propertyDefinition.setDataClass(expected);
            if ( sampleValue != null) {
                // actually going to be setting this property
                flowPropertyProvider.addPropertyDefinitions(propertyDefinition);
            }
        }
        getFlowTranslatorResolver().resolve(propertyDefinition);
        getLog().warn("FlowState: Creating a dynamic FlowDefinition for key="+key+" might want to check situation. FlowState="+this );
        return propertyDefinition;
    }
    /**
     * @see org.amplafi.flow.FlowManagement#getInstanceFromDefinition(java.lang.String)
     */
    public Flow getInstanceFromDefinition(String flowTypeName) {
        return getFlowManager().getInstanceFromDefinition(flowTypeName);
    }
    /**
     * @see org.amplafi.flow.FlowManagement#registerForCacheClearing()
     */
    public void registerForCacheClearing() {

    }

    public void setFlowTx(FlowTx flowTx) {
        this.flowTx = flowTx;
    }

    public FlowTx getFlowTx() {
        return flowTx;
    }
    public Log getLog() {
        return LogFactory.getLog(this.getClass());
    }

    public void setFlowManager(FlowManager flowManager) {
        this.flowManager = flowManager;
    }

    public FlowManager getFlowManager() {
        return flowManager;
    }

    /**
     * @param flowTranslatorResolver the flowTranslatorResolver to set
     */
    public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
        this.flowTranslatorResolver = flowTranslatorResolver;
    }

    /**
     * @return the flowTranslatorResolver
     */
    public FlowTranslatorResolver getFlowTranslatorResolver() {
        return flowTranslatorResolver;
    }


    /**
     * @see org.amplafi.flow.FlowManagement#getFlowPropertyDefinition(java.lang.String)
     */
    @Override
    public FlowPropertyDefinition getFlowPropertyDefinition(String key) {
        return this.getFlowTranslatorResolver().getFlowPropertyDefinition(key);
    }


    /**
     * @see org.amplafi.flow.FlowManagement#getDefaultHomePage()
     */
    @Override
    public URI getDefaultHomePage() {
        return this.getFlowManager().getDefaultHomePage();
    }

    /**
     * @param pageProvider the pageProvider to set
     */
    public void setPageProvider(PageProvider pageProvider) {
        this.pageProvider = pageProvider;
    }

    /**
     * @return the pageProvider
     */
    public PageProvider getPageProvider() {
        return pageProvider;
    }

    protected static class SessionFlows implements Iterable<FlowStateImplementor>{
        private LinkedList<FlowStateImplementor> activeFlows = new LinkedList<FlowStateImplementor>();
        private Map<String, FlowStateImplementor> activeFlowsMap = new ConcurrentHashMap<String, FlowStateImplementor>();
        public synchronized boolean remove(FlowStateImplementor flowState) {
            activeFlowsMap.values().remove(flowState);
            return activeFlows.remove(flowState);
        }
        /**
         * @param lookupKey
         * @return the removed {@link FlowState} with the supplied lookupKey.
         */
        public synchronized FlowState removeByLookupKey(String lookupKey) {
            FlowState flowState = activeFlowsMap.remove(lookupKey);
            activeFlows.remove(flowState);
            return flowState;
        }
        /**
         * @return true if no active flows.
         */
        public synchronized boolean isEmpty() {
            return activeFlows.isEmpty();
        }
        public boolean add(FlowStateImplementor flowState) {
            addLast(flowState);
            return true;
        }
        public synchronized FlowStateImplementor getFirst() {
            return isEmpty()?null:activeFlows.getFirst();
        }
        public synchronized void addLast(FlowStateImplementor flowState) {
            activeFlowsMap.put(flowState.getLookupKey(), flowState);
            activeFlows.add(flowState);
        }
        public synchronized void makeFirst(FlowStateImplementor flowState) {
            if ( getFirst() == flowState) {
                return;
            } else if ( activeFlowsMap.containsKey(flowState)) {
                activeFlows.remove(flowState);
            }
            activeFlowsMap.put(flowState.getLookupKey(), flowState);
            activeFlows.addFirst(flowState);
        }
        public synchronized int makeAfter(FlowStateImplementor flowState, FlowStateImplementor nextFlowState) {
            if(!activeFlowsMap.containsKey(flowState.getLookupKey())) {
                throw new IllegalStateException(flowState.getLookupKey()+ ": not a current flow");
            }
            int oldPosition = -1;
            for(int i = 0; i < this.activeFlows.size(); ) {
                FlowStateImplementor state = this.activeFlows.get(i);
                if ( state == nextFlowState) {
                    oldPosition = i;
                    this.activeFlows.remove(i);
                } else {
                    if ( state == flowState) {
                        this.add(++i, nextFlowState);
                    }
                    i++;
                }
            }
            return oldPosition;
        }
        /**
         * @param i
         * @param nextFlowState
         */
        public synchronized void add(int i, FlowStateImplementor nextFlowState) {
            this.activeFlows.add(i, nextFlowState);
            this.activeFlowsMap.put(nextFlowState.getLookupKey(), nextFlowState);
        }
        public synchronized FlowState get(String lookupKey) {
            if (lookupKey == null ) {
                throw new IllegalArgumentException("lookupKey for flow is null!");
            }
            return this.activeFlowsMap.get(lookupKey);
        }
        /**
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public synchronized Iterator<FlowStateImplementor> iterator() {
            return this.activeFlows.iterator();
        }

        @Override
        public String toString() {
            return "Session Flows : " + join(this.activeFlows, ", ");
        }
    }
}
