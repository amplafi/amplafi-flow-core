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
import java.util.Set;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowState;

/**
 * @author patmoore
 *
 */
public interface FlowPropertyDefinitionImplementor extends FlowPropertyDefinition {

    FlowPropertyDefinition initialize();

    <FA extends FlowPropertyProvider> void setFlowPropertyValueProvider(FlowPropertyValueProvider<FA> flowPropertyValueProvider);

    /**
     * @return collection of property that if changed should invalidate this property.
     */
    @Override
    Set<FlowPropertyExpectation> getPropertiesDependentOn();

    /**
     * The namespace used to retrieve this property while the flowState is actively running after the flowState's FlowValueMap has been initialized.
     *  ( using the namespaces listed in {@link #getNamespaceKeySearchList(FlowState, FlowPropertyProvider, boolean)} )
     *
     * @param flowState
     * @param flowPropertyProvider
     * @return namespace
     */
    String getNamespaceKey(FlowState flowState, FlowPropertyProvider flowPropertyProvider);

    /**
     * the list of namespaces used to find the property value in the FlowState map when INITIALIZING or EXPORTING the flowState's FlowValueMap
     * This list is constructed by examining the PropertyUsage constraints.
     * The list returned is in most specific namespace to most general. This is important for property initialization when initializing a FlowState.
     * @param flowState (may be null )
     * @param flowPropertyProvider (may be null )
     * @param forceAll TODO
     * @return ordered collection used to find/set this property.
     */
    List<String> getNamespaceKeySearchList(FlowState flowState, FlowPropertyProvider flowPropertyProvider, boolean forceAll);
}
