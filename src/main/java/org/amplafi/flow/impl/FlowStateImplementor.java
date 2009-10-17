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

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowStateLifecycle;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowState;

/**
 * @author patmoore
 */
public interface FlowStateImplementor extends FlowState {
    <T> void setPropertyWithDefinition(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition, T value);

    <T> T getPropertyWithDefinition(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition);

    String getRawProperty(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition);

    Long getRawLong(FlowActivity flowActivity, String key);
    @Deprecated
    String getRawProperty(String key);

    String getRawProperty(String namespace, String key);

    /**
     *
     * @param key
     * @param value
     * @return true if the value has changed.
     */
    @Deprecated
    boolean setRawProperty(String key, String value);

    /**
     * @param flowActivityImplementor
     * @param flowPropertyDefinition
     */
    void initializeFlowProperty(FlowActivityImplementor flowActivityImplementor, FlowPropertyDefinition flowPropertyDefinition);

    void initializeFlowProperties(FlowActivityImplementor flowActivity, Iterable<FlowPropertyDefinition> flowPropertyDefinitions);

    /**
     * get FlowActivity by position. It is preferred to use {@link #getActivity(String)}
     *
     * @param <T>
     * @param activityIndex
     * @return flowActivity
     */
    <T extends FlowActivity> T getActivity(int activityIndex);


    void setFlowLifecycleState(FlowStateLifecycle flowStateLifecycle);

    /**
     * Called when this flowstate no longer represents the current flow. Assume
     * that this FlowState may be G.C.'ed
     */
    void clearCache();

    void setCached(String namespace, String key, Object value);
    void setCached(FlowPropertyDefinition flowPropertyDefinition, FlowActivity flowActivity, Object value);

    <T> T getCached(String namespace, String key);

    /**
     * @param flowManagement The flowManagement to set.
     */
    void setFlowManagement(FlowManagement flowManagement);


    /**
     * @param flowPropertyDefinition
     * @param flowActivity
     * @return
     */
    <T> T getCached(FlowPropertyDefinition flowPropertyDefinition, FlowActivity flowActivity);
}
