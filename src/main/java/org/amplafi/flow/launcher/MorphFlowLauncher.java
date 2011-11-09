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
package org.amplafi.flow.launcher;

import java.util.Map;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;


/**
 * This FlowLauncher allows the flowstate to change the flow it is running.
 */

public class MorphFlowLauncher extends BaseFlowLauncher implements ListableFlowLauncher {

    public MorphFlowLauncher(String flowTypeName, String lookupKey, FlowManagement flowManagement) {
        this(flowTypeName, lookupKey, null, flowManagement);
    }

    public MorphFlowLauncher(String flowTypeName, String lookupKey,
            Map<String, String> initialFlowState, FlowManagement flowManagement) {
        super(flowTypeName, flowManagement, initialFlowState, lookupKey /*see comment in FlowTransition.*/);
        this.setExistingFlowStateLookupKey(lookupKey);
    }

    @Override
    public FlowState call() {
        FlowState currentFlowState = getFlowState();
        currentFlowState.morphFlow(getFlowTypeName(), getInitialFlowState());
        return currentFlowState;
    }
}
