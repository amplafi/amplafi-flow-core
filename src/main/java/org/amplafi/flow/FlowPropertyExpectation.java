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

import java.util.List;

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.flowproperty.FlowPropertyValueChangeListener;
import org.amplafi.flow.flowproperty.FlowPropertyValuePersister;
import org.amplafi.flow.flowproperty.PropertyScope;
import org.amplafi.flow.flowproperty.PropertyUsage;

import com.sworddance.util.map.MapKeyed;

/**
 * @author patmoore
 *
 */
public interface FlowPropertyExpectation extends MapKeyed<String> {
    String getName();
    /**
     * The class of the object returned by a call to a {@link org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#getProperty(String, Class)}
     * For a complex collection definition this is the top-level collection.
     *
     * This is optional and is only enforced if a non-null is returned.
     * @return the class expected if defined.
     */
    Class<?> getDataClass();
    /**
     * For more complex type definitions, such as collections and maps, this
     * @return
     */
    DataClassDefinition getDataClassDefinition();

    /**
     * @return how the property is being used.
     */
    PropertyUsage getPropertyUsage();

    PropertyScope getPropertyScope();

    <FA extends FlowPropertyProvider> FlowPropertyValueProvider<FA> getFlowPropertyValueProvider();
    /**
     *
     * @return the {@link FlowActivityPhase} when the property be required to be available.
     */
    FlowActivityPhase getPropertyRequired();

    /**
     * @return listeners to be notified when the property changes value.
     */
    List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners();

    boolean isApplicable(FlowPropertyDefinitionImplementor flowPropertyDefinition);

    // Kostya: switch the definitions here:
    <FA extends FlowPropertyProvider> FlowPropertyValuePersister<FA> getFlowPropertyValuePersister();
//    FlowPropertyValuePersister<?> getFlowPropertyValuePersister();
}
