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

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor;
import org.amplafi.flow.launcher.ValueFromBindingProvider;

import com.sworddance.beans.ClassResolver;

import org.apache.commons.logging.Log;


/**
 * Implementations are stored in a user session. There will be one instance / user session.
 * @author Patrick Moore
 *
 */
public interface FlowManagement extends FlowStateListener {
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
     * a user may have multiple concurrently active
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
     * @param returnToFlow must be passed on starting a flow because the flow may run to completion once started.
     * @return the newly-started FlowState
     */
    <FS extends FlowState> FS startFlowState(String flowTypeName, boolean currentFlow, Map<String, String> initialFlowState, Object returnToFlow);
    <FS extends FlowState> FS startFlowState(String flowTypeName, boolean currentFlow, Map<String, String> initialFlowState);

    /**
     * Continue the flow with the given lookup key.
     * @param <FS>
     * @param lookupKey
     * @param currentFlow
     * @param initialFlowState
     * @return the resulting FlowState may not be the same as the FlowState corresponding to the passed lookupKey. This
     * happens if the lookupKey'ed flow completes.
     */
    <FS extends FlowState> FS continueFlowState(String lookupKey, boolean currentFlow, Map<String, String> initialFlowState);
    /**
     * the flows that the current session has active.
     *
     * @return list of active {@link FlowState}s referenced by this FlowManagement object.
     */
    List<FlowState> getFlowStates();

    /**
     * drop the indicated flow. This is not a canceling of the flow
     * With "Cancel" a flow has a chance to do something in response.
     * A flow is dropped after it has completed, but can also be dropped if
     * the user is stuck.
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

    FlowImplementor getInstanceFromDefinition(String flowTypeName);

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
     * @param nextFlowLifecycleState
     * @return the page that should be rendered next.
     */
    String completeFlowState(FlowState flowState, boolean newFlowActive, FlowStateLifecycle nextFlowLifecycleState);

    /**
     * @return the FlowTranslatorResolver
     */
    FlowTranslatorResolver getFlowTranslatorResolver();

    /**
     * Do the Dependency Injection on this activity.
     * @param object may be null.
     */
    void wireDependencies(Object object);

    /**
     * look for a global {@link FlowPropertyDefinition} that is not specific to a {@link Flow} or {@link FlowActivity}.
     * @param propertyName
     * @param dataClass TODO
     */
    FlowPropertyDefinitionBuilder getFactoryFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass);

    /**
     * {@link FlowManagement} is a session
     * object the value returned by this method may be customized to the session setting.
     * @return the default home to use when a flow ends and there is no other place to return.
     */
    @Deprecated
    URI getDefaultHomePage();

    <T> FlowPropertyDefinitionImplementor createFlowPropertyDefinition(FlowPropertyProviderImplementor flowPropertyProvider, String key, Class<T> expected, T sampleValue);

    void addFlowStateListener(FlowStateListener flowStateListener);

    /**
     * @return the ValueFromBindingProvider - needed so the FlowLaunchers can get this service
     */
    ValueFromBindingProvider getValueFromBindingProvider();

    ClassResolver getClassResolver();
}