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

import org.amplafi.flow.definitions.DefinitionSource;


/**
 * FlowDefinitionsManager handles flow definitions.
 *
 * @author Patrick Moore
 */
public interface FlowDefinitionsManager extends DefinitionSource {

    void addDefinitions(FlowImplementor... flows);

    /**
     * @param key (usually) the {@link Flow#getFlowPropertyProviderName()}.
     * @param flow the flow definition to add.
     */
    void addDefinition(String key, FlowImplementor flow);

    void addDefinitionSource(DefinitionSource definitionSource);

}