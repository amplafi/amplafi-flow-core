/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.amplafi.flow.definitions;
import java.util.Map;

/**
 * Simple Flow {@link DefinitionSource}.
 *
 * @author patmoore
 * @param <F>
 */
public class MapDefinitionSource<F extends FlowDefinition> implements DefinitionSource<F> {
    private Map<String, F> flowDefinitions;

    /**
     * Constructor.
     */
    public MapDefinitionSource() {
    }

    /**
     * @param flowDefinitions - map  of flow name to definition.
     */
    public MapDefinitionSource(Map<String, F> flowDefinitions) {
        this.flowDefinitions = flowDefinitions;
    }

    /**
     * @see org.amplafi.flow.definitions.
     *                      DefinitionSource#getFlowDefinition(java.lang.String)
     */
    public F getFlowDefinition(String flowTypeName) {
        return this.flowDefinitions != null ? this.flowDefinitions.get(flowTypeName) : null;
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
     */
    public boolean isFlowDefined(String flowTypeName) {
        return this.flowDefinitions != null ? this.flowDefinitions.containsKey(flowTypeName) : false;
    }

    /**
     * @param flowDefinitions the flowDefinitions to set
     */
    public void setFlowDefinitions(Map<String, F> flowDefinitions) {
        this.flowDefinitions = flowDefinitions;
    }

    /**
     * @return the flowDefinitions
     */
    public Map<String, F> getFlowDefinitions() {
        return flowDefinitions;
    }
}
