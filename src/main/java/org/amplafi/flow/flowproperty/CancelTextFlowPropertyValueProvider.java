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

package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowValuesMap;

import static org.amplafi.flow.FlowConstants.*;
import static org.amplafi.flow.flowproperty.PropertyScope.*;
import static org.amplafi.flow.flowproperty.PropertyUsage.*;
import static org.apache.commons.lang.StringUtils.*;
/**
 * @author patmoore
 *
 */
public class CancelTextFlowPropertyValueProvider extends AbstractFlowPropertyValueProvider<FlowActivityImplementor> implements FlowPropertyDefinitionProvider {
    public static final CancelTextFlowPropertyValueProvider INSTANCE = new CancelTextFlowPropertyValueProvider();

    public CancelTextFlowPropertyValueProvider() {
        super(FSCANCEL_TEXT);
    }
    /**
     *
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.flowproperty.FlowPropertyProvider, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowActivityImplementor flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        check(flowPropertyDefinition);
        String label = "message:flow.label-cancel";
        String lookupKey =flowActivity.getProperty(FSRETURN_TO_FLOW);
        if ( lookupKey != null ) {
            FlowManagement flowManagement = flowActivity.getFlowState().getFlowManagement();
            FlowState flowState = flowManagement.getFlowState(lookupKey);
            if ( flowState != null) {
                label = flowState.getCurrentActivity().getProperty(FSRETURN_TO_TEXT);
                if (isBlank(label)) {
                    // TODO -- how to internationalize?
                    label = "Return to "+flowState.getFlowTitle();
                }
            }
        }
        return (T) label;
    }

    /**
     * @see org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider#defineFlowPropertyDefinitions(org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor, org.amplafi.flow.FlowValuesMap)
     */
    @Override
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, FlowValuesMap additionalConfigurationParameters) {
        this.addPropertyDefinitions(flowPropertyProvider,
            new FlowPropertyDefinitionImpl(FSCANCEL_TEXT).initAccess(flowLocal, use));
    }

}
