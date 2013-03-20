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

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;



/**
 * FlowManager is a factory for creating and managing the standard {@link FlowManagement} implementations.
 *
 * @author Patrick Moore
 */
public interface FlowManager {

    FlowImplementor getInstanceFromDefinition(String flowTypeName);

    /**
     * Returns the flow having the specified name.
     * @param flowTypeName
     * @return the Flow definition.
     */
    Flow getFlowDefinition(String flowTypeName);
    boolean isFlowDefined(String flowTypeName);
    FlowPropertyDefinitionBuilder getFactoryFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass);
    /**
     * TODO: Need to define if the FlowManagement object is saved by FlowManager implementations.
     * if so how? per session? per thread?
     * @param <FM>
     * @return (possibly new) FlowManagement object.
     */
    <FM extends FlowManagement> FM getFlowManagement();

    Collection<String> listAvailableFlows();
}