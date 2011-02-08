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
package org.amplafi.flow.impl;

import java.util.Map;

import org.amplafi.flow.FlowGroup;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.definitions.DefinitionSource;
import org.amplafi.flow.definitions.MapDefinitionSource;
import org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor;

/**
 *
 * @author patmoore
 *
 */
public class FlowGroupImpl extends BaseFlowPropertyProvider<FlowImplementor> implements FlowGroup, FlowPropertyProviderImplementor {

    private DefinitionSource<FlowImplementor> definitionSource = new MapDefinitionSource();

    private FlowGroup primaryFlowGroup;

	private String name;

    /**
     *
     */
    public FlowGroupImpl() {
        super();
    }
    /**
    *
    */
   public FlowGroupImpl(String name) {
       super();
       this.name = name;
   }

    /**
     * @param definition
     */
    public FlowGroupImpl(FlowImplementor definition) {
        super(definition);
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinition(java.lang.String)
     */
    public FlowImplementor getFlowDefinition(String flowTypeName) {
        return definitionSource.getFlowDefinition(flowTypeName);
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinitions()
     */
    public Map<String, FlowImplementor> getFlowDefinitions() {
        return definitionSource.getFlowDefinitions();
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
     */
    public boolean isFlowDefined(String flowTypeName) {
        return definitionSource.isFlowDefined(flowTypeName);
    }

    @Override
    public <FS extends FlowState> FS getFlowState() {
        // flow groups do not have a FlowState.
        return null;
    }

    /**
     * @param primaryFlowGroup the primaryFlowGroup to set
     */
    public void setPrimaryFlowGroup(FlowGroup primaryFlowGroup) {
        this.primaryFlowGroup = primaryFlowGroup;
    }

    /**
     * @return the primaryFlowGroup
     */
    public FlowGroup getPrimaryFlowGroup() {
        return primaryFlowGroup;
    }

    /**
     * @param definitionSource the definitionSource to set
     */
    public void setDefinitionSource(DefinitionSource definitionSource) {
        this.definitionSource = definitionSource;
    }

    /**
     * @return the definitionSource
     */
    public DefinitionSource getDefinitionSource() {
        return definitionSource;
    }

}
