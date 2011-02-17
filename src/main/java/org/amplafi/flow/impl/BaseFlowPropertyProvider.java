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

import java.util.LinkedHashMap;
import java.util.Map;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;

import com.sworddance.util.ApplicationIllegalArgumentException;
import static com.sworddance.util.CUtilities.*;
/**
 * @author patmoore
 * @param <FPP>
 *
 */
public abstract class BaseFlowPropertyProvider<FPP extends FlowPropertyProvider> implements FlowPropertyProvider {

    private FPP definition;

    private Map<String, FlowPropertyDefinition> propertyDefinitions;

    protected String flowPropertyProviderName;

    private transient boolean resolved;

    public BaseFlowPropertyProvider() {
    }
    /**
     * @param definition
     */
    public BaseFlowPropertyProvider(FPP definition) {
        this.definition = definition;
    }
    public BaseFlowPropertyProvider(String flowPropertyProviderName) {
        this.flowPropertyProviderName = flowPropertyProviderName;
    }
    public boolean isInstance() {
        return this.definition != null;
    }

    protected FPP getDefinition() {
        return definition;
    }

    protected void setDefinition(FPP definition) {
        this.definition = definition;
    }

    public void setPropertyDefinitions(Map<String, FlowPropertyDefinition> properties) {
        this.propertyDefinitions = properties;
    }

    @SuppressWarnings("unchecked")
    public <FD extends FlowPropertyDefinition> Map<String, FD> getPropertyDefinitions() {
        if ( propertyDefinitions == null && this.isInstance() ) {
            // as is usually the case for instance flow activities.
            return this.definition.getPropertyDefinitions();
        }
        return (Map<String, FD>) propertyDefinitions;
    }
    /**
     * Look for the {@link FlowPropertyDefinition} in just this FlowPropertyProvider's local property definitions map or its definition.
     * Do not follow the chain of other FlowPropertyProviders.
     * @param flowPropertyDefinitionName
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <FPD extends FlowPropertyDefinition> FPD getFlowPropertyDefinition(String flowPropertyDefinitionName) {
        return (FPD) getFlowPropertyDefinition(flowPropertyDefinitionName, true);
    }
    /**
     *
     * @param <FPD>
     * @param flowPropertyDefinitionName
     * @param followChain in future will have the chain following currently only in FlowActivity. But want to have this extra argument so the
     * some FlowActivity code can be pulled up.
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <FPD extends FlowPropertyDefinition> FPD getFlowPropertyDefinition(String flowPropertyDefinitionName, boolean followChain) {
        Map<String, FlowPropertyDefinition> propDefs = this.getPropertyDefinitions();
        return (FPD)( propDefs == null? null : propDefs.get(flowPropertyDefinitionName));
    }
    @SuppressWarnings("unchecked")
    protected <FPD extends FlowPropertyDefinition> FPD putLocalPropertyDefinition(FlowPropertyDefinition flowPropertyDefinition) {
        if ( this.getPropertyDefinitions() == null ) {
            this.setPropertyDefinitions(new LinkedHashMap<String, FlowPropertyDefinition>());
            if ( isInstance()) {
                // adding instance specific properties so copy the definition's FlowPropertyDefinitions for modification.
                if ( isNotEmpty(this.definition.getPropertyDefinitions())) {
                    this.getPropertyDefinitions().putAll(this.definition.getPropertyDefinitions());
                }
            }
        }
        return (FPD) getPropertyDefinitions().put(flowPropertyDefinition.getName(), flowPropertyDefinition);
    }
    @SuppressWarnings("unchecked")
    protected <FPD extends FlowPropertyDefinition> FPD removeLocalPropertyDefinition(String flowPropertyDefinitionName) {
        if ( getPropertyDefinitions() != null ) {
            return (FPD) getPropertyDefinitions().remove(flowPropertyDefinitionName);
        } else {
            return null;
        }
    }
    /**
     * method used by hivemind to add in properties
     * @param flowPropertyDefinition
     * @see org.amplafi.flow.flowproperty.FlowPropertyProviderImplementor#addPropertyDefinition(org.amplafi.flow.FlowPropertyDefinition)
     */
    public void addPropertyDefinition(FlowPropertyDefinition flowPropertyDefinition) {
        if ( flowPropertyDefinition == null ) {
            return;
        }

        FlowPropertyDefinition current = this.getFlowPropertyDefinition(flowPropertyDefinition.getName(), false);
        if ( current != null ) {
            if ( !flowPropertyDefinition.merge(current) ) {
                throw new ApplicationIllegalArgumentException(this,".",flowPropertyDefinition,": cannot be merged with ",current);
            }
        }
        putLocalPropertyDefinition(flowPropertyDefinition);
    }
    public void addPropertyDefinitions(Iterable<FlowPropertyDefinition> flowPropertyDefinitions) {
        if ( flowPropertyDefinitions != null ) {
            for (FlowPropertyDefinition flowPropertyDefinition : flowPropertyDefinitions) {
                addPropertyDefinition(flowPropertyDefinition);
            }
        }
    }
    public void addPropertyDefinitions(FlowPropertyDefinition... flowPropertyDefinitions) {
        if ( flowPropertyDefinitions != null && flowPropertyDefinitions.length > 0) {
            for(FlowPropertyDefinition flowPropertyDefinition: flowPropertyDefinitions) {
                this.addPropertyDefinition(flowPropertyDefinition);
            }
        }
    }

    public boolean isFlowPropertyProviderNameSet() {
        return this.flowPropertyProviderName != null;
    }
    public String getFlowPropertyProviderName() {
        return this.flowPropertyProviderName;
    }

    public void setFlowPropertyProviderName(String flowPropertyProviderName) {
        this.flowPropertyProviderName = flowPropertyProviderName;
    }

    public String getFlowPropertyProviderFullName() {
        return getFlowPropertyProviderName();
    }
    /**
     * @param resolved the resolved to set
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    /**
     * @return the resolved
     */
    public boolean isResolved() {
        return resolved;
    }
}
