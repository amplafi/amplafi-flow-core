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

import java.util.Map;

import org.amplafi.flow.FlowPropertyDefinition;

/**
 * Implementers manage a map of {@link FlowPropertyDefinition}s
 *
 * NOTE: use of this interface needs clarification wrt to the FlowPropertyValueProvider interface.
 * Specifically, FlowPropertyProvider implementors:
 * 1. can be queried to find out the value for multiple properties. (so this would be FlowStates, Flow, FlowActivities ).
 * 2. delegate to FlowPropertyValueProvider to find any missing values.
 * 3. store values in a cache.
 *
 * probably this interface must have the get/set value methods here rather than on a subinterface.
 * @author patmoore
 *
 */
public interface FlowPropertyProvider {
    /**
     *
     * @return just the immediate name not the full context. May not be unique.
     */
    String getFlowPropertyProviderName();

    /**
     * "flowgroup.flowName.activityName"
     * "flowgroup.flowName"
     *
     * @return full name, must be unique.
     */
    String getFlowPropertyProviderFullName();

    <FD extends FlowPropertyDefinition> Map<String, FD> getPropertyDefinitions();

    <FD extends FlowPropertyDefinition> FD getFlowPropertyDefinition(String key);

}
