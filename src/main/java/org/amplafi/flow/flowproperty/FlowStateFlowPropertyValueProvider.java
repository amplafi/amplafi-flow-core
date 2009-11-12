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

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.impl.FlowStateImplementor;
import static org.amplafi.flow.FlowConstants.*;

/**
 * FlowPropertyValueProvider that does flowState lookup.
 * @author patmoore
 *
 */
public class FlowStateFlowPropertyValueProvider extends AbstractFlowPropertyValueProvider<FlowActivity> implements FlowPropertyDefinitionProvider {

    public static final FlowStateFlowPropertyValueProvider INSTANCE = new FlowStateFlowPropertyValueProvider();
    /**
     * @see org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider#defineFlowPropertyDefinitions(FlowPropertyProviderImplementor)
     */
    @Override
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider) {
        addPropertyDefinitions(flowPropertyProvider,
            new FlowPropertyDefinitionImpl(FSRETURN_TO_FLOW, FlowStateImplementor.class).initAccess(PropertyScope.flowLocal, PropertyUsage.io),
            new FlowPropertyDefinitionImpl(FSCONTINUE_WITH_FLOW, FlowStateImplementor.class).initAccess(PropertyScope.flowLocal, PropertyUsage.io)
            );
    }

    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        FlowStateImplementor flowStateImplementor = flowActivity.getFlowState();
        String lookupKey = flowStateImplementor.getRawProperty(flowActivity, flowPropertyDefinition);
        FlowState flowState = flowStateImplementor.getFlowManagement().getFlowState(lookupKey);
        return (T) flowState;
    }

}
