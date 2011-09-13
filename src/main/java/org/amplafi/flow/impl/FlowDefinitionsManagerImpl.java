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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.sworddance.util.CUtilities.*;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowDefinitionsManager;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.definitions.DefinitionSource;
import org.amplafi.flow.definitions.XmlDefinitionSource;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.MissingRequiredTracking;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationIllegalStateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic implementation for managing Flow definitions. Provides no persistence mechanism.
 *
 */
public class FlowDefinitionsManagerImpl implements FlowDefinitionsManager {
    private FlowTranslatorResolver flowTranslatorResolver;
    private boolean running;
    private ConcurrentMap<String, FlowImplementor> flowDefinitions;
    private List<String> flowsFilenames;
    /**
     * This map is used to connect a standard property name i.e. "user" to a standard class (UserImpl)
     * This map solves the problem where a flowProperty*Provider or changelistener needs (or would like)
     * to have a property available but does not define it.
     *
     * Explicit NOTE: the flow propertydefinitions returned MUST not persist any changes to permanent storage.
     * This is easy to enforce at the primary level (i.e. no persister is called. ) but what about accessing a read-only property
     * that returns a db object and then changes the db object? Can we tell hibernate not to persist?
     */
    private Map<String, FlowPropertyDefinitionImplementor> standardPropertyNameToDefinition;

    private Log log;
    public FlowDefinitionsManagerImpl() {
        flowDefinitions = new ConcurrentHashMap<String, FlowImplementor>();
        this.standardPropertyNameToDefinition = new ConcurrentHashMap<String, FlowPropertyDefinitionImplementor>();
        flowsFilenames = new CopyOnWriteArrayList<String>();
    }

    /**
     *
     */
    public void initializeService() {
        for(String fileName: getFlowsFilenames()) {
            XmlDefinitionSource definitionSource = new XmlDefinitionSource(fileName);
            addDefinitions(definitionSource);
        }
        initFlowDefinitions();
        running = true;
    }

    /**
     *
     */
    private void initFlowDefinitions() {
        Collection<FlowImplementor> flowDefinitionCollection = flowDefinitions.values();
        for(FlowImplementor flow: flowDefinitionCollection) {
            getFlowTranslatorResolver().resolveFlow(flow);
        }
    }

    @Override
    public void addDefinitions(DefinitionSource... definitionSources) {
        for(DefinitionSource<FlowImplementor> definitionSource: definitionSources) {
            Collection<FlowImplementor> flows = definitionSource.getFlowDefinitions().values();
            for(FlowImplementor flow: flows) {
                addDefinition(flow);
            }
        }
    }
    public void addDefinition(FlowImplementor flow) {
        ApplicationIllegalStateException.checkState(!flow.isInstance(), flow, " is an instance not a definition");

        this.flowTranslatorResolver.resolveFlow(flow);
        getFlowDefinitions().put(flow.getFlowPropertyProviderFullName(), flow);
    }

    /**
     * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinition(java.lang.String)
     */
    @Override
    public FlowImplementor getFlowDefinition(String flowTypeName) {
        ApplicationIllegalArgumentException.notNull(flowTypeName, "null flowTypeName");
        FlowImplementor flow = this.getFlowDefinitions().get(flowTypeName);
        if (flow==null) {
            throw new FlowValidationException("flow.definition-not-found", new MissingRequiredTracking(flowTypeName));
        }
        return flow;
    }

    /**
     * @see org.amplafi.flow.FlowDefinitionsManager#getFlowDefinitions()
     */
    @Override
    public Map<String, FlowImplementor> getFlowDefinitions() {
        return this.flowDefinitions;
    }

    public void setFlowDefinitions(Map<String, FlowImplementor> flowDefinitions) {
        this.flowDefinitions.clear();
        if ( isNotEmpty(flowDefinitions) ) {
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
     *
     * @param propertyName
     * @param standardDefinitionClass
     */
    public void addStandardPropertyDefinition(String propertyName, Class<?> standardDefinitionClass) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = new FlowPropertyDefinitionImpl(propertyName, standardDefinitionClass);
    	addStandardPropertyDefinition(flowPropertyDefinition);
    }

    /**
     * This property should be minimal.
     * @param flowPropertyDefinition supply the default property.
     */
    public void addStandardPropertyDefinition(FlowPropertyDefinitionImplementor flowPropertyDefinition) {
    	String propertyName = flowPropertyDefinition.getName();
    	ApplicationIllegalStateException.checkState(!this.standardPropertyNameToDefinition.containsKey(propertyName), propertyName, " already defined as a standard property.");
    	flowPropertyDefinition.setTemplateFlowPropertyDefinition();
    	this.standardPropertyNameToDefinition.put(propertyName, flowPropertyDefinition);
    	// Note: alternate names are not automatically added.
    }

    /**
     * Used as the way to get a property
     * @param propertyName
     * @param dataClass
     * @return
     */
    public FlowPropertyDefinitionBuilder getFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass) {
    	FlowPropertyDefinitionImplementor standardDefinition = this.standardPropertyNameToDefinition.get(propertyName);
    	if ( standardDefinition == null || (dataClass != null && !standardDefinition.isAssignableFrom(dataClass))) {
    		return new FlowPropertyDefinitionBuilder().createFlowPropertyDefinition(propertyName, dataClass);
    	} else {
    		return new FlowPropertyDefinitionBuilder(standardDefinition);
    	}
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

    /**
     * @param flowsFilenames the flowsFilenames to set
     */
    public void setFlowsFilenames(List<String> flowsFilenames) {
        this.flowsFilenames.clear();
        this.flowsFilenames.addAll(flowsFilenames);
    }

    /**
     * @return the flowsFilenames
     */
    public List<String> getFlowsFilenames() {
        return flowsFilenames;
    }
}
