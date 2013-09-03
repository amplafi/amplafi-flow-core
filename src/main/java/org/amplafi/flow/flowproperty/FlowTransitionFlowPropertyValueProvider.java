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

import static org.amplafi.flow.FlowConstants.FSFLOW_TRANSITION;
import static org.amplafi.flow.flowproperty.PropertyScope.flowLocal;
import static org.amplafi.flow.flowproperty.PropertyUsage.initialize;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowTransition;
/**
 * TODO: Take over the services provided by {@link org.amplafi.flow.impl.TransitionFlowActivity}
 * @author patmoore
 *
 */
public class FlowTransitionFlowPropertyValueProvider extends AbstractFlowPropertyValueProvider<FlowPropertyProvider> {

	public static final FlowPropertyDefinitionImplementor FLOW_TRANSITION = new FlowPropertyDefinitionBuilder(FSFLOW_TRANSITION, FlowTransition.class).initAccess(flowLocal, initialize).toFlowPropertyDefinition();
    public FlowTransitionFlowPropertyValueProvider() {
        super(FLOW_TRANSITION);
    }

    @Override
    public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        check(flowPropertyDefinition);
        throw new UnsupportedOperationException();
    }

}
