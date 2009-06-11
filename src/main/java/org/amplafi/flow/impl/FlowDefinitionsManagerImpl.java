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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.MissingRequiredTracking;
import org.amplafi.flow.FlowDefinitionsManager;
import org.amplafi.flow.Flow;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 */
public class FlowDefinitionsManagerImpl implements FlowDefinitionsManager {
    private FlowTranslatorResolver flowTranslatorResolver;
    private boolean running;
    private ConcurrentMap<String, Flow> flowDefinitions;
    private Log log;
    public FlowDefinitionsManagerImpl() {
        flowDefinitions = new ConcurrentHashMap<String, Flow>();
    }

    /**
     *
     */
    public void initializeService() {
        initFlowDefinitions();
        running = true;
    }

    /**
     *
     */
    private void initFlowDefinitions() {
        Collection<Flow> flowDefinitionCollection = flowDefinitions.values();
        for(Flow flow: flowDefinitionCollection) {
            getFlowTranslatorResolver().resolveFlow(flow);
        }
    }
    /**
     * @see org.amplafi.flow.FlowDefinitionsManager#addDefinitions(Flow...)
     */
    @Override
    public void addDefinitions(Flow... flows) {
        for(Flow flow: flows) {
            addDefinition(flow.getFlowTypeName(), flow);
        }
    }
    public void addDefinition(String key, Flow flow) {
        if (flow.isInstance()) {
            throw new IllegalStateException( flow+ " not a definition");
        }
        this.flowTranslatorResolver.resolveFlow(flow);
        getFlowDefinitions().put(key, flow);
    }
    /**
     * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinition(java.lang.String)
     */
    @Override
    public Flow getFlowDefinition(String flowTypeName) {
        if ( flowTypeName == null) {
            throw new IllegalArgumentException("null flowTypeName");
        }
        Flow flow = this.getFlowDefinitions().get(flowTypeName);
        if (flow==null) {
            throw new FlowValidationException("flow.definition-not-found", new MissingRequiredTracking(flowTypeName));
        }
        return flow;
    }

    /**
     * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinitions()
     */
    @Override
    public Map<String, Flow> getFlowDefinitions() {
        return this.flowDefinitions;
    }

    public void setFlowDefinitions(Map<String, Flow> flowDefinitions) {
        this.flowDefinitions.clear();
        if ( MapUtils.isNotEmpty(flowDefinitions) ) {
            this.flowDefinitions.putAll(flowDefinitions);
        }
        if ( running) {
            initFlowDefinitions();
        }
    }

    public Log getLog() {
        if ( this.log == null ) {
            this.log = LogFactory.getLog(this.getClass());
        }
        return this.log;
    }

    /**
     * @see org.amplafi.flow.FlowDefinitionsManager#isFlowDefined(java.lang.String)
     */
    @Override
    public boolean isFlowDefined(String flowTypeName) {
        Flow flow = this.getFlowDefinitions().get(flowTypeName);
        return flow != null;
    }
    public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
        this.flowTranslatorResolver = flowTranslatorResolver;
    }
    public FlowTranslatorResolver getFlowTranslatorResolver() {
        return flowTranslatorResolver;
    }
}
