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

import static org.apache.commons.lang.StringUtils.*;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.impl.FlowStateImplementor;



/**
 * This FlowLauncher continues a previously created FlowState.
 * @author Patrick Moore
 */
public class ContinueFlowLauncher extends BaseFlowLauncher implements ListableFlowLauncher {

    private static final long serialVersionUID = -1221490458104629351L;
    public ContinueFlowLauncher() {

    }
    public ContinueFlowLauncher(FlowState flowState, FlowManagement flowManagement) {
        super(flowState, flowManagement, flowState.getLookupKey());
    }

    /**
     * May return no FlowState if the FlowState represented by
     * the lookupKey was already completed.
     * @see org.amplafi.flow.launcher.FlowLauncher#call()
     * @return the flow that was continued
     */
    public FlowState call() {
        return getFlowManagementWithCheck().continueFlowState(getExistingFlowStateLookupKey(), true, this.getInitialFlowState());
    }
    @Override
    public String getLinkTitle() {
        String linkLabel = null;
        if ( getFlowState() != null ) {
            linkLabel = getFlowState().getActiveFlowLabel();
        }
        if ( isBlank(linkLabel)) {
            linkLabel = super.getLinkTitle();
        }
        return linkLabel;
    }

    @Override
    public void setLinkTitle(String label) {
        if ( getFlowState() != null ) {
            ((FlowStateImplementor)getFlowState()).setActiveFlowLabel(label);
        }
        super.setLinkTitle(label);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getInitialFlowState() {
        return getFlowState().getFlowValuesMap().getAsFlattenedStringMap();
    }
}
