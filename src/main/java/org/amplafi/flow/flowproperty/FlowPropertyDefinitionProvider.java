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



import java.util.List;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;


/**
 * holds map of {@link FlowPropertyDefinitionBuilder}s (not {@link FlowPropertyDefinition}s).
 * Converting to FlowPropertyDefinitions forced all the unspecified attributes to be defined.
 * But in the FlowPropertyDefinitionProvider we are not yet ready to make that decision. We need to wait until putting the
 * properties on a specific {@link FlowPropertyProvider} ( and the additional {@link FlowPropertyExpectation}s can be applied )
 * before forcing all the unspecified attributes to a concrete setting.
 *
 * Implementers will use the {@link FlowPropertyDefinitionBuilder}s to generate the needed FlowPropertyDefinitions to {@link FlowPropertyProvider}s.
 *
 * A Flow Property is, once defined, a typed java 'attribute' that is managed by the flow framework. It has a scope of validity
 * ({@link PropertyScope}), and other properties that might be altered by using a {@link FlowPropertyDefinitionBuilder} when you define
 * the property. Depending on where you're standing, you have various interfaces to fetch them using {@link FlowPropertyValueProvider}
 * or accessing - in a lower level - directly on a k/v map using getProperty, for example inside a {@link FlowActivity}
 *
 * Before applied the {@link FlowPropertyDefinitionBuilder} must be cloned to avoid changing the stored definition.
 *
 * @author patmoore
 *
 */
public interface FlowPropertyDefinitionProvider {

    /**
     * Add to the flowPropertyProvider the definitions supplied by this FlowPropertyDefinitionProvider
     *
     * TODO: the wiring has not happened on this object when the define is called
     * @param flowPropertyProvider
     */
    void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider);

    /**
     *
     * @param flowPropertyProvider
     * @param additionalConfigurationParameters
     */
    void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation>additionalConfigurationParameters);
    FlowPropertyDefinitionBuilder getFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass);

    /**
     *
     * @return list of property names that this are outputted.
     */
    List<String> getOutputFlowPropertyDefinitionNames();
}
