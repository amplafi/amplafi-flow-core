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
import org.amplafi.flow.FlowImplementor;

/**
 * Simple Flow {@link DefinitionSource}
 * @author patmoore
 *
 */
public class MapDefinitionSource implements DefinitionSource {
    private Map<String, FlowImplementor> flowDefinitions;

    public MapDefinitionSource() {

    }
    /**
     * @param flowDefinitions
     */
    public MapDefinitionSource(Map<String, FlowImplementor> flowDefinitions) {
        this.flowDefinitions = flowDefinitions;
    }
    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinition(java.lang.String)
     */
    @Override
    public FlowImplementor getFlowDefinition(String flowTypeName) {
        return this.flowDefinitions != null? this.flowDefinitions.get(flowTypeName):null;
    }


    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
     */
    @Override
    public boolean isFlowDefined(String flowTypeName) {
        return this.flowDefinitions != null?  this.flowDefinitions.containsKey(flowTypeName): false;
    }


    /**
     * @param flowDefinitions the flowDefinitions to set
     */
    public void setFlowDefinitions(Map<String, FlowImplementor> flowDefinitions) {
        this.flowDefinitions = flowDefinitions;
    }


    /**
     * @return the flowDefinitions
     */
    public Map<String, FlowImplementor> getFlowDefinitions() {
        return flowDefinitions;
    }

}
