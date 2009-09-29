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
 * the direction the flow is moving.
 * @author patmoore
 *
 */
public enum FlowStepDirection {
    forward,
    backward,
    inPlace;

    /**
     * Determine the FlowStepDirection based on the starting FlowActivity index and the next FlowActivity index.
     * @param currentFlowActivityIndex
     * @param endingFlowActivityIndex
     * @return the FlowStepDirection
     */
    public static FlowStepDirection get(int currentFlowActivityIndex, int endingFlowActivityIndex) {
        if ( currentFlowActivityIndex == endingFlowActivityIndex) {
            return inPlace;
        } else if ( currentFlowActivityIndex > endingFlowActivityIndex) {
            return backward;
        } else {
            return forward;
        }
    }
}
