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
 * @author patmoore
 *
 */
public interface FlowPropertyProvider {
    /**
     *
     * @return unique portion of the namespace.
     */
    String getFlowPropertyProviderName();

    /**
     * "flowName.activityName"
     *
     * @return full flow activity name.
     */
    String getFlowPropertyProviderFullName();

    <FD extends FlowPropertyDefinition> Map<String, FD> getPropertyDefinitions();

    <FD extends FlowPropertyDefinition> FD getFlowPropertyDefinition(String key);

}
