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
import org.amplafi.flow.FlowValueMapKey;
import org.amplafi.flow.FlowValuesMap;


/**
 * Implementers will add the needed FlowPropertyDefinitions to {@link FlowPropertyProvider}s.
 *
 * The {@link FlowPropertyDefinition}s added are not to be shared. ( YET )
 *
 * TODO: Allow immutable {@link FlowPropertyDefinition} to be returned.
 * @author patmoore
 *
 */
public interface FlowPropertyDefinitionProvider {

    /**
     * Add to the flowPropertyProvider the definitions supplied by this FlowPropertyDefinitionProvider
     *
     * TODO: the wiring has not happened on this object when the define is called
     * @param flowPropertyProvider
     * @param additionalConfigurationParameters some FlowPropertyDefinitionProvider need additional configuration parameters.
     */
    void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, FlowValuesMap<? extends FlowValueMapKey, ? extends CharSequence> additionalConfigurationParameters);


}
