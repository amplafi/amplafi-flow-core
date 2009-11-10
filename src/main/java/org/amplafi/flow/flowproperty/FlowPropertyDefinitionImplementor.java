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
    <FA extends FlowActivity> void setFlowPropertyValueProvider(FlowPropertyValueProvider<FA> flowPropertyValueProvider);
    <FA extends FlowActivity> void setFlowPropertyValuePersister(FlowPropertyValuePersister<FA> flowPropertyValuePersister);
    <V> V parse(String value) throws FlowException;
}
