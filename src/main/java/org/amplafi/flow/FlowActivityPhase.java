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

/**
 * @author patmoore
 *
 */
public enum FlowActivityPhase {
    optional,
    /**
     * FlowProperty is required to be set before calling the {@link org.amplafi.flow.FlowActivity#activate(org.amplafi.flow.FlowStepDirection)}.
     * Use for properties that must be set by prior steps for the FlowActivity to function correctly.
     *
     * Properties of this sort should be minimized. "activate" properties impose an ordering that may not
     * be the best for a user experience and restrict a product manager's ability to redesign a flow.
     */
    activate,
    /**
     * FlowProperty is required to be set before advancing beyond the {@link org.amplafi.flow.FlowActivity}.
     * This is used when *the* current FlowActivity is requesting the user to set the property to a value.
     *
     * The FlowActivity must have the ability to let user set the property in this case.
     * Do not use as a gatekeeper to 'protect' later FlowActivities.
     */
    advance,
    /**
     * FlowProperty is required to be set prior to calling the {@link org.amplafi.flow.FlowActivity#saveChanges()}.
     *
     * This is used in cases where the FlowActivity can gather its needed data and perform correctly
     * until the moment that the changes need to be saved.
     */
    saveChanges,
    /**
     * FlowProperty is required to be set prior to calling the {@link org.amplafi.flow.FlowActivity#finishFlow(org.amplafi.flow.FlowState)}.
     */
    finish,
    /**
     * The FlowActivity will create it.
     */
    @Deprecated
    creates
}
