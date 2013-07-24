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
 * Signals at which stage in Flow processing a property must be set or can have a default value successfully set via
 * {@link FlowPropertyDefinition#getDefaultObject(org.amplafi.flow.flowproperty.FlowPropertyProvider)}
 *
 * @author patmoore
 *
 */
public enum FlowActivityPhase {
    /**
     * FlowProperty is required to be set before calling the {@link org.amplafi.flow.FlowActivity#activate(org.amplafi.flow.FlowStepDirection)}.
     * Use for properties that must be set by prior steps for the FlowActivity to function correctly.
     *
     * Properties of this sort should be minimized. "activate" properties impose an ordering that may not
     * be the best for a user experience and restrict a product manager's ability to redesign a flow.
     */
    activate(false),
    /**
     * FlowProperty is required to be set before advancing beyond the {@link org.amplafi.flow.FlowActivity}.
     * This is used when *the* current FlowActivity is requesting the user to set the property to a value.
     *
     * The FlowActivity must have the ability to let user set the property in this case ( i.e. proper UI component)
     * Do not use as a gatekeeper to 'protect' later FlowActivities, those later FlowActivities will have
     */
    advance(true),
    /**
     * FlowProperty is required to be set prior to calling the {@link org.amplafi.flow.FlowActivity#saveChanges()}.
     *
     * This is used in cases where the FlowActivity can gather its needed data and perform correctly
     * until the moment that the changes need to be saved.
     */
    saveChanges(true),
    /**
     * FlowProperty is required to be set prior to calling the {@link org.amplafi.flow.FlowActivity#finishFlow(org.amplafi.flow.FlowState)}.
     */
    finish(true),
    /**
     * explicit declaration that the FlowProperty is optional.
     */
    optional(false);
    private final boolean advancing;
    FlowActivityPhase(boolean advancing) {
        this.advancing = advancing;
    }
    /**
     * @return true if the {@link FlowActivityPhase} means the current activity may be advanced past.
     * see also {@link FlowStepDirection}.
     */
    public boolean isAdvancing() {
        return advancing;
    }
    public boolean isAdvancing(FlowStepDirection flowStepDirection) {
        return isAdvancing() && flowStepDirection != FlowStepDirection.backward;
    }
    public static boolean isSameAs(FlowActivityPhase flowActivityPhase1, FlowActivityPhase flowActivityPhase2) {
        if ( flowActivityPhase1 == flowActivityPhase2) {
            return true;
        } else if ( flowActivityPhase1 == null && flowActivityPhase2 == optional) {
            // null is same as optional
            return true;
        } else if ( flowActivityPhase1 == optional && flowActivityPhase2 == null) {
            // null is same as optional
            return true;
        }
        return false;
    }
    /**
     * will be used to resolve
     * @param flowActivityPhase1
     * @param flowActivityPhase2
     * @return the flowActivityPhase that comes earlier in the flow cycle.
     */
    public static FlowActivityPhase earliest(FlowActivityPhase flowActivityPhase1, FlowActivityPhase flowActivityPhase2) {
        if (flowActivityPhase1 == null || flowActivityPhase1 == flowActivityPhase2) {
            return flowActivityPhase2;
        } else if ( flowActivityPhase2 == null ) {
            return flowActivityPhase1;
        } else if ( flowActivityPhase1.ordinal() < flowActivityPhase2.ordinal()) {
            return flowActivityPhase1;
        } else {
            return flowActivityPhase2;
        }
    }
}
