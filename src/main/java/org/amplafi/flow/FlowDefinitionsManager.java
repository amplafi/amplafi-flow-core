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

import java.util.Collection;

import org.amplafi.flow.definitions.DefinitionSource;
import org.amplafi.flow.definitions.FlowDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilderProvider;


/**
 * FlowDefinitionsManager handles flow definitions.
 *
 * @author Patrick Moore
 */
public interface FlowDefinitionsManager extends DefinitionSource<FlowImplementor> {

    /**
     * @param flow the flow definition to add.
     */
    void addDefinition(FlowImplementor flow);

    @SuppressWarnings("unchecked")
    void addDefinitions(DefinitionSource<? extends FlowDefinition>... definitionSource);

    /**
     *
     * @param propertyName
     * @param dataClass propertyType returned must be of this type
     * @return
     */
    FlowPropertyDefinitionBuilder getFactoryFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass);

    void addFactoryFlowPropertyDefinitionBuilderProvider(FlowPropertyDefinitionBuilderProvider factoryFlowPropertyDefinitionBuilderProvider);
    void addFactoryFlowPropertyDefinitionBuilderProviders(Collection<FlowPropertyDefinitionBuilderProvider> factoryFlowPropertyDefinitionBuilderProviders);
}