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
package org.amplafi.flow;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.impl.FlowStateImplementor;
import org.apache.commons.logging.Log;


/**
 * implementation that provides session level FlowManagement.
 * @author Patrick Moore
 *
 */
public interface FlowManagement {

    public static final String USER_INFORMATION = "sessionFlows";

    /**
     *  a user may have multiple concurrently active
     * requests.
     * @param <FS>
     *
     * @param lookupKey
     * @return the flow named with this flowId.
     */
    <FS extends FlowState> FS getFlowState(String lookupKey);

    /**
     *  a user may have multiple concurrently active
     * requests.
     * @param <FS>
     *
     * @param flowTypes
     * @return the first active {@link FlowState} having the given type.
     */
    <FS extends FlowState> FS getFirstFlowStateByType(String... flowTypes);
    /**
     * synchronized because a user may have multiple concurrently active
     * requests.
     * @param <FS>
     *
     * @param flowTypes
     * @return the first active {@link FlowState} having the given type.
     */
    <FS extends FlowState> FS getFirstFlowStateByType(Collection<String> flowTypes);
    /**
     * a user may have multiple concurrently active
     * requests.
     *
     * @param flowType
     * @return the active {@link FlowState}s having the given type.
     */
    List<FlowState> getActiveFlowStatesByType(String... flowType);

    <FS extends FlowState> FS getCurrentFlowState();

    /**
     * creates a {@link FlowState}. The properties are set to the initialFlowState.
     * @param <FS>
     *
     * @param flowTypeName
     * @param initialFlowState
     * @param makeNewStateCurrent
     * @return the newly-created FlowState
     */
    <FS extends FlowState> FS createFlowState(String flowTypeName, Map<String, String> initialFlowState, boolean makeNewStateCurrent);

    <FS extends FlowState> FS createFlowState(String flowTypeName, FlowState initialFlowState, Map<String, String> initialValues, boolean makeNewStateCurrent);

    /**
     * Starts a flow by name.
     * @param <FS>
     * @param flowTypeName The name of the flow.
     * @param currentFlow Whether to make this the current active flow.
     * @param initialFlowState The initial state of the flow.
     * @param returnToFlow TODO
     * @return the newly-started FlowState
     */
    <FS extends FlowState> FS startFlowState(String flowTypeName, boolean currentFlow,
            Map<String, String> initialFlowState, Object returnToFlow);

    /**
     * Starts a flow by name.
     * @param <FS>
     * @param flowType
     * @param currentFlow the new flow should be set the current flow.
     * @param propertyRoot
     * @param initialValues used to define the initial values for flow. This is a
     * list of strings. Each string is 'key=value'. if value is the same name as a component
     * that has a 'value' attribute (like TextField components) then the initial value.
     * If value is a container's property then that value is used. Otherwise the value
     * provided is used as a literal.
     * @param returnToFlow
     * @return the newly-started FlowState
     */
    <FS extends FlowState> FS startFlowState(String flowType, boolean currentFlow, Object propertyRoot,
            Iterable<String> initialValues, Object returnToFlow);

    /**
     * Continue the flow with the given lookup key.
     * @param <FS>
     * @param lookupKey
     * @param currentFlow
     * @param propertyRoot
     * @param initialValues
     * @return the resulting FlowState may not be the same as the FlowState corresponding to the passed lookupKey. This
     * happens if the lookupKey'ed flow completes.
     */
    <FS extends FlowState> FS continueFlowState(String lookupKey, boolean currentFlow, Object propertyRoot,
            Iterable<String> initialValues);
    /**
     * Continue the flow with the given lookup key.
     * @param <FS>
     * @param lookupKey
     * @param currentFlow
     * @param initialFlowState
     * @return the resulting FlowState may not be the same as the FlowState corresponding to the passed lookupKey. This
     * happens if the lookupKey'ed flow completes.
     */
    <FS extends FlowState> FS continueFlowState(String lookupKey, boolean currentFlow,
            Map<String, String> initialFlowState);
    /**
     * the flows that the current session has active.
     *
     * @return list of active {@link FlowState}s referenced by this FlowManagement object.
     */
    List<FlowState> getFlowStates();

    /**
     * drop the indicated flow. This is not a canceling of the flow
     * as a flow is dropped after it has completed.
     * @param flow
     * @return the page name to land on.
     */
    String dropFlowState(FlowState flow);

    /**
     *
     * @param <FS>
     * @param flowState
     * @param key that will return a Map<String, {@link FlowTransition} > the flowState getFinishType will be used as key into this map.
     * @return the flow transitioning to.
     */
    <FS extends FlowState> FS transitionToFlowState(FlowState flowState, String key);

    /**
     * drop the indicated flow. This is not canceling the flow.
     * With "Cancel" a flow has a chance to do something in response.
     * A flow is dropped after it has completed, but can also be dropped if
     * the user is stuck.
     * @param lookupKey
     * @return the page name to land on.
     */
    String dropFlowStateByLookupKey(String lookupKey);

    FlowActivity getCurrentActivity();

    Flow getInstanceFromDefinition(String flowTypeName);

    void registerForCacheClearing();

    Log getLog();

    /**
     * @param flowState
     * @param nextFlowState
     * @return true if the current flow changed ( nextFlowState was the current flow)
     */
    boolean makeAfter(FlowState flowState, FlowState nextFlowState);

    /**
     * @param state the {@link FlowState} that should be made the current FlowState.
     * @return the {@link #getCurrentFlowState()}.{@link FlowState#getCurrentPage()}, null if no current flow.
     */
    String makeCurrent(FlowState state);

    Flow getFlowDefinition(String flowTypeName);

    /**
     * @return the current transaction.
     */
    FlowTx getFlowTx();

    /**
     *
     * @return the {@link #getCurrentFlowState()}.{@link FlowState#getCurrentPage()}, null if no current flow.
     */
    String getCurrentPage();

    /**
     * flow has exited. Called by {@link FlowState#finishFlow()}
     * @param flowState
     * @param newFlowActive
     * @return the page that should be rendered next.
     */
    String completeFlowState(FlowState flowState, boolean newFlowActive);

    /**
     * @return the FlowTranslatorResolver
     */
    FlowTranslatorResolver getFlowTranslatorResolver();

    /**
     * Do the Dependency Injection on this activity.
     * @param activity may be null.
     */
    void resolveFlowActivity(FlowActivity activity);

    /**
     * look for a global {@link FlowPropertyDefinition} that is not specific to a {@link Flow} or {@link FlowActivity}.
     * @param key
     * @return the global {@link FlowPropertyDefinition}
     */
    FlowPropertyDefinition getFlowPropertyDefinition(String key);

    /**
     * similar to {@link FlowManager#getDefaultHomePage()}. However, because {@link FlowManagement} is a session
     * object the value returned by this method may be customized to the session settings and may not be
     * {@link FlowManager#getDefaultHomePage()}.
     * @return the default home to use when a flow ends and there is no other place to return.
     */
    URI getDefaultHomePage();

    <T> FlowPropertyDefinition createFlowPropertyDefinition(FlowPropertyProvider flowPropertyProvider, String key, Class<T> expected, T sampleValue);

    void addFlowLifecycleListener(FlowLifecycleStateListener flowLifecycleStateListener);

    /**
     *
     * @param flowState
     * @param previousFlowLifecycleState
     */
    void notifyFlowLifecycleListeners(FlowStateImplementor flowState, FlowLifecycleState previousFlowLifecycleState);

}