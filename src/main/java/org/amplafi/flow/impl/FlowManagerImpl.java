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

import java.util.Collection;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowDefinitionsManager;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowManager;
import org.amplafi.flow.FlowStateListener;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;

import com.sworddance.util.ApplicationIllegalArgumentException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.apache.commons.collections.CollectionUtils.*;

/**
 *
 *
 */
public class FlowManagerImpl implements FlowManager {
    private FlowTranslatorResolver flowTranslatorResolver;
    private FlowDefinitionsManager flowDefinitionsManager;

    private transient Set<FlowStateListener> flowStateListeners = Collections.synchronizedSet(new HashSet<FlowStateListener>());
    private Log log;

    public FlowManagerImpl() {

    }

    @Inject
    public FlowManagerImpl(FlowTranslatorResolver flowTranslatorResolver, FlowDefinitionsManager flowDefinitionsManager) {
        this.setFlowTranslatorResolver(flowTranslatorResolver);
        this.setFlowDefinitionsManager(flowDefinitionsManager);
    }
    /**
     * @see org.amplafi.flow.FlowManager#getInstanceFromDefinition(java.lang.String)
     */
    @Override
    public FlowImplementor getInstanceFromDefinition(String flowTypeName) {
        FlowImplementor definition = flowDefinitionsManager.getFlowDefinition(flowTypeName);
        ApplicationIllegalArgumentException.notNull(definition,
            flowTypeName, ": Flow definition with this name is not defined.");
        FlowImplementor inst = definition.createInstance();
        return inst;
    }

    public Log getLog() {
        if ( this.log == null ) {
            this.log = LogFactory.getLog(this.getClass());
        }
        return this.log;
    }

    /**
     * @see org.amplafi.flow.FlowManager#getFlowManagement()
     */
    @Override
    public FlowManagement getFlowManagement() {
        BaseFlowManagement baseFlowManagement = new BaseFlowManagement();
        baseFlowManagement.setFlowManager(this);
        baseFlowManagement.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return baseFlowManagement;
    }

    /**
     * @see org.amplafi.flow.FlowDefinitionsManager#isFlowDefined(java.lang.String)
     */
    @Override
    public boolean isFlowDefined(String flowTypeName) {
        return this.getFlowDefinitionsManager().isFlowDefined(flowTypeName);
    }
    @Override
    public FlowPropertyDefinitionBuilder getFactoryFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass) {
        return this.getFlowDefinitionsManager().getFactoryFlowPropertyDefinitionBuilder(propertyName, dataClass);
    }
    @Override
    public Flow getFlowDefinition(String flowTypeName) {
        return this.getFlowDefinitionsManager().getFlowDefinition(flowTypeName);
    }
    public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
        this.flowTranslatorResolver = flowTranslatorResolver;
    }
    public FlowTranslatorResolver getFlowTranslatorResolver() {
        return flowTranslatorResolver;
    }
    /**
     * @param flowStateListeners the flowStateListeners to set
     */
    public void setFlowStateListeners(Set<FlowStateListener> flowStateListeners) {
        this.flowStateListeners.clear();
        if ( isNotEmpty(flowStateListeners)) {
            this.flowStateListeners.addAll(flowStateListeners);
        }
    }

    /**
     * @return the flowStateListeners
     */
    public Set<FlowStateListener> getFlowStateListeners() {
        return flowStateListeners;
    }
    public void addFlowStateListener(FlowStateListener flowStateListener) {
        this.getFlowStateListeners().add(flowStateListener);
    }

    /**
     * @param flowDefinitionsManager the flowDefinitionsManager to set
     */
    public void setFlowDefinitionsManager(FlowDefinitionsManager flowDefinitionsManager) {
        this.flowDefinitionsManager = flowDefinitionsManager;
    }

    /**
     * @return the flowDefinitionsManager
     */
    public FlowDefinitionsManager getFlowDefinitionsManager() {
        return flowDefinitionsManager;
    }

    @Override
    public Collection<String> listAvailableFlows() {
        return getFlowDefinitionsManager().getFlowDefinitions().keySet();
    }
}
