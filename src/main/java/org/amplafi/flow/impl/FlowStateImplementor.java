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

import java.util.Map;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowStateLifecycle;
import org.amplafi.flow.FlowValuesMap;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;

/**
 * @author patmoore
 */
public interface FlowStateImplementor extends FlowState {
    <T> void setPropertyWithDefinition(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinitionImplementor flowPropertyDefinition, T value);

    <T> T getPropertyWithDefinition(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinitionImplementor flowPropertyDefinition);

    String getRawProperty(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition);

    String getRawProperty(String namespace, String key);

    /**
     * @param flowPropertyProvider
     * @param flowPropertyDefinition
     */
    void initializeFlowProperty(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinitionImplementor flowPropertyDefinition);

    void initializeFlowProperties(FlowPropertyProvider flowPropertyProvider, Iterable<FlowPropertyDefinitionImplementor> flowPropertyDefinitions);

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
     * set the page that the flow should tell the UI to go to when this flow completes.
     * sets the {@link org.amplafi.flow.FlowConstants#FSAFTER_PAGE} property.
     * @see org.amplafi.flow.FlowConstants#FSDEFAULT_AFTER_PAGE
     * @see org.amplafi.flow.FlowConstants#FSDEFAULT_AFTER_CANCEL_PAGE
     *
     * @param afterPage
     */
    void setAfterPage(String afterPage);
    /**
     * set the {@link org.amplafi.flow.FlowConstants#FSPAGE_NAME}
     * @param currentPage
     */
    void setCurrentPage(String currentPage);
    void setCancelText(String cancelText);
    void setFinishText(String finishText);

    /**
     * Called when this flowstate no longer represents the current flow. Assume
     * that this FlowState may be G.C.'ed
     */
    void clearCache();

    void setCached(String namespace, String key, Object value);
    void setCached(FlowPropertyDefinitionImplementor flowPropertyDefinition, FlowPropertyProvider flowPropertyProvider, Object value);

    <T> T getCached(String namespace, String key);

    /**
     * @param flowManagement The flowManagement to set.
     */
    void setFlowManagement(FlowManagement flowManagement);

    void setFlowValuesMap(FlowValuesMap flowValuesMap);

    /**
     * @param <T>
     * @param flowPropertyDefinition
     * @param flowPropertyProvider
     * @return cached value
     */
    <T> T getCached(FlowPropertyDefinitionImplementor flowPropertyDefinition, FlowPropertyProvider flowPropertyProvider);

    /**
     * This bypasses ALL security checks. It *must* be only used for cases when the values are known to be safe. (i.e.
     * nothing that came from the clients.)
     *
     * @param trustedValues
     */
    void copyTrustedValuesMapToFlowState(Map<String, String> trustedValues);
}
