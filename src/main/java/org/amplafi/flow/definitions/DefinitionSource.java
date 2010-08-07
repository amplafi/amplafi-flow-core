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
package org.amplafi.flow.definitions;

import java.util.Map;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowImplementor;

/**
 * Implementers will provide Flow definitions.
 *
 * TODO should be namespace owner.
 * @author patmoore
 *
 */
public interface DefinitionSource {

    /**
     * Returns the flow having the specified name.
     * @param flowTypeName
     * @return the Flow definition. null if {@link #isFlowDefined(String)} returns false.
     */
    FlowImplementor getFlowDefinition(String flowTypeName);
    /**
     * does this DefinitionSource have a definition for a flow with supplied name.
     * @param flowTypeName
     * @return true if definition exists.
     */
    boolean isFlowDefined(String flowTypeName);

    /**
     * Returns all defined flows, keyed by their name.
     * @return the map with all the currently defined flows indexed by (usually) the {@link Flow#getFlowPropertyProviderName()}.
     */
    Map<String, FlowImplementor> getFlowDefinitions();
}
