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

import java.util.Map;


/**
 * FlowDefinitionsManager handles flow definitions.
 *
 * @author Patrick Moore
 */
public interface FlowDefinitionsManager {

    void addDefinitions(Flow... flows);

    /**
     * Returns the flow having the specified name.
     * @param flowTypeName
     * @return the Flow definition.
     */
    Flow getFlowDefinition(String flowTypeName);
    boolean isFlowDefined(String flowTypeName);

    /**
     * Returns all defined flows, keyed by their name.
     * @return the map with all the currently defined flows indexed by (usually) the {@link Flow#getFlowTypeName()}.
     */
    Map<String, Flow> getFlowDefinitions();

    /**
     * @param key (usually) the {@link Flow#getFlowTypeName()}.
     * @param flow the flow definition to add.
     */
    void addDefinition(String key, Flow flow);
}