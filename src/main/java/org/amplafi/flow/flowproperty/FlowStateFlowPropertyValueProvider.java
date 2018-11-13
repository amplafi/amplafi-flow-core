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


import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.impl.FlowStateImplementor;

import static org.amplafi.flow.FlowConstants.*;

import java.util.Optional;

/**
 * FlowPropertyValueProvider that does flowState lookup.
 * @author patmoore
 *
 */
public class FlowStateFlowPropertyValueProvider extends AbstractFlowPropertyValueProvider<FlowPropertyProviderImplementor> implements FlowPropertyDefinitionBuilderProvider {

    public static final FlowStateFlowPropertyValueProvider INSTANCE = new FlowStateFlowPropertyValueProvider();

    public FlowStateFlowPropertyValueProvider() {
        super(// TODO -- add a FLOW_STATE property ...
                new FlowPropertyDefinitionBuilder(FSRETURN_TO_FLOW, FlowStateImplementor.class).initAccess(PropertyScope.flowLocal, PropertyUsage.io),
                new FlowPropertyDefinitionBuilder(FSRETURN_TO_FLOW_TYPE, String.class).initAccess(PropertyScope.flowLocal, PropertyUsage.io),
                new FlowPropertyDefinitionBuilder(FSCONTINUE_WITH_FLOW, FlowStateImplementor.class).initAccess(PropertyScope.flowLocal, PropertyUsage.io));
    }
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowPropertyProviderImplementor flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        FlowStateImplementor flowStateImplementor = flowPropertyProvider.getFlowState();
        // HACK should be 'deserialized'
        Optional<String> optLookupKey = flowStateImplementor.getRawProperty(flowPropertyProvider, flowPropertyDefinition);
        return (T) optLookupKey.map(lookupKey ->
                    flowStateImplementor.getFlowManagement().getFlowState(lookupKey)
                ).orElse(null);
    }
}
