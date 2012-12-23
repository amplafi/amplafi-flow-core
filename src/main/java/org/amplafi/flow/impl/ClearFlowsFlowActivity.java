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

import java.util.ArrayList;
import java.util.List;

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.FlowState;


/**
 * A FlowActivity that can be used to clear other flowStates other than the current FlowState
 * which this {@link org.amplafi.flow.FlowActivity} is in.
 *
 */
public class ClearFlowsFlowActivity extends FlowActivityImpl {

    /**
     *
     */
    private static final String FLOWS = "flows";
    @Override
    protected void addStandardFlowPropertyDefinitions() {
        super.addStandardFlowPropertyDefinitions();
        addPropertyDefinitions(new FlowPropertyDefinitionImpl(FLOWS, List.class));
    }

    @Override
    public void initializeFlow() {
        super.initializeFlow();
        if ( !isPropertySet(FLOWS)) {
            // we don't want to try to clear ourselves.
            ArrayList<String> l = new ArrayList<String>();
            for(FlowState fs: this.getFlowManagement().getFlowStates()) {
                if ( getFlowState() != fs) {
                    l.add(fs.getLookupKey());
                }
            }
            initPropertyIfNull(FLOWS, l);
        }
    }
    @Override
    public FlowState finishFlow(FlowState currentNextFlowState) {
        FlowState nextFlowState = super.finishFlow(currentNextFlowState);
        List<String> flowStateLookupKeys = getProperty(FLOWS);
        String currentLookupKey = this.getFlowState().getLookupKey();
        for (String lookupKey: flowStateLookupKeys) {
            if ( lookupKey.equals(currentLookupKey) ||
                    nextFlowState != null && lookupKey.equals(nextFlowState.getLookupKey()) ) {
                continue;
            } else {
                getFlowManagement().dropFlowState(getFlowManagement().getFlowState(lookupKey));
            }
        }
        return nextFlowState;
    }
}
