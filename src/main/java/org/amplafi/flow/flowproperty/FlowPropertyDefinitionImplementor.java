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

import java.util.Collection;

import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * @author patmoore
 *
 */
public interface FlowPropertyDefinitionImplementor extends FlowPropertyDefinition {

    <T> String serialize(T object);

    FlowPropertyDefinition initialize();

    void setPropertyRequired(FlowActivityPhase flowActivityPhase);

    FlowPropertyDefinitionImplementor initFlowPropertyValuePersister(FlowPropertyValuePersister<?> flowPropertyValuePersister);

    FlowPropertyDefinitionImplementor initFlowPropertyValueChangeListener(FlowPropertyValueChangeListener flowPropertyValueChangeListener);
    FlowPropertyDefinitionImplementor initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider);
    <FA extends FlowPropertyProvider> void setFlowPropertyValueProvider(FlowPropertyValueProvider<FA> flowPropertyValueProvider);
    <FA extends FlowPropertyProvider> void setFlowPropertyValuePersister(FlowPropertyValuePersister<FA> flowPropertyValuePersister);
    <V> V parse(FlowPropertyProvider flowPropertyProvider, String value) throws FlowException;

    void setPropertyScope(PropertyScope propertyScope);

    void setPropertyUsage(PropertyUsage propertyUsage);
    <T extends FlowPropertyDefinitionImplementor> T clone();

    /**
     * @return collection of property that if chagned should invalidate this property.
     */
    Collection<String> getPropertiesDependentOn();
}
