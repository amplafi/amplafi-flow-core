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

import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;



/**
 * This FlowLauncher continues a previously created FlowState.
 * @author Patrick Moore
 */
public class ContinueFlowLauncher extends BaseFlowLauncher implements ListableFlowLauncher {

    private static final long serialVersionUID = -1221490458104629351L;
    public ContinueFlowLauncher() {

    }
    public ContinueFlowLauncher(FlowState flowState, FlowManagement flowManagement) {
        super(flowState.getFlowTypeName(), flowManagement, null, flowState.getLookupKey());
        setExistingFlowStateLookupKey(flowState.getLookupKey());
    }
    public ContinueFlowLauncher(String flowTypeName, String existingFlowStateLookupKey) {
        super(flowTypeName, null, null, existingFlowStateLookupKey);
        setExistingFlowStateLookupKey(existingFlowStateLookupKey);
    }
    /**
     * May return no FlowState if the FlowState represented by
     * the lookupKey was already completed.
     * @see org.amplafi.flow.launcher.FlowLauncher#call()
     * @return the flow that was continued
     */
    @Override
    public FlowState call() {
        // TODO: We should not need? to pass the flowState because this flow is already known (reapplying same values???)
        FlowState flowState = getFlowState();
        if ( flowState == null ) {
            throw new FlowException("No flowState with id:", getExistingFlowStateLookupKey());
        } else {
            return getFlowManagementWithCheck().continueFlowState(getExistingFlowStateLookupKey(), true, this.getInitialFlowState());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getInitialFlowState() {
        return getFlowState().getFlowValuesMap().getAsFlattenedStringMap();
    }
}
