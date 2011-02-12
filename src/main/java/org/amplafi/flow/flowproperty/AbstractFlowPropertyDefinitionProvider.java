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
package org.amplafi.flow.flowproperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;

import com.sworddance.util.NotNullIterator;

/**
 * support methods for {@link FlowPropertyDefinitionProvider} implementations.
 * Since this class does not implement any methods in {@link FlowPropertyDefinitionProvider} this class does not implement {@link FlowPropertyDefinitionProvider}.
 * @author patmoore
 *
 */
public abstract class AbstractFlowPropertyDefinitionProvider {
    private List<FlowPropertyDefinitionImplementor> flowPropertyDefinitions;
    protected AbstractFlowPropertyDefinitionProvider(FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        setFlowPropertyDefinitions(flowPropertyDefinitions);
    }
    /**
     * @param flowPropertyDefinitions
     */
    protected void setFlowPropertyDefinitions(FlowPropertyDefinitionImplementor... flowPropertyDefinitions) {
        this.flowPropertyDefinitions = new ArrayList<FlowPropertyDefinitionImplementor>();
        for(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor: NotNullIterator.<FlowPropertyDefinitionImplementor>newNotNullIterator(flowPropertyDefinitions)) {
        	flowPropertyDefinitionImplementor.setTemplateFlowPropertyDefinition();
        	this.flowPropertyDefinitions.add(flowPropertyDefinitionImplementor);
        }
    }
    /**
     * @return the flowPropertyDefinitions
     */
    public List<FlowPropertyDefinitionImplementor> getFlowPropertyDefinitions() {
        return flowPropertyDefinitions;
    }

    /**
     * add ALL the {@link FlowPropertyDefinition}s provided by this definition provider to flowPropertyProvider.
     * @param flowPropertyProvider
     * @param additionalConfigurationParameters a list because order is significant
     */
    protected void addDefinedPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        if ( this.flowPropertyDefinitions != null) {
            List<FlowPropertyDefinitionImplementor> clonedFlowPropertyDefinitions = new ArrayList<FlowPropertyDefinitionImplementor>();
            for(FlowPropertyDefinitionImplementor flowPropertyDefinition: flowPropertyDefinitions) {
            	// TODO: cloning aggressively will not be necessary when FlowPropertyDefinitionImplementor does a better job of being immutable.
                FlowPropertyDefinitionImplementor cloned = flowPropertyDefinition.clone();
                clonedFlowPropertyDefinitions.add(cloned);
            }
            this.addPropertyDefinitions(flowPropertyProvider, clonedFlowPropertyDefinitions, additionalConfigurationParameters);
        }
    }
    /**
     * adds in the initFlowPropertyValueProvider(this) since I keep forgetting.
     * @param flowPropertyProvider
     * @param flowPropertyDefinitions
     */
    protected void addPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider,Collection<FlowPropertyDefinitionImplementor>flowPropertyDefinitions, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        for(FlowPropertyDefinitionImplementor flowPropertyDefinition: flowPropertyDefinitions) {
        	FlowPropertyDefinitionImplementor returnedFlowPropertyDefinition = initPropertyDefinition(flowPropertyProvider, flowPropertyDefinition, additionalConfigurationParameters);
            flowPropertyProvider.addPropertyDefinitions(returnedFlowPropertyDefinition);
        }
    }
    /**
     * initialize a flowPropertyDefinition.
     * @param flowPropertyProvider
     * @param flowPropertyDefinition will be modified (make sure not modifying the master definition)
     * @param additionalConfigurationParameters
     */
    protected FlowPropertyDefinitionImplementor initPropertyDefinition(
        FlowPropertyProviderImplementor flowPropertyProvider,
        final FlowPropertyDefinitionImplementor flowPropertyDefinition, List<FlowPropertyExpectation> additionalConfigurationParameters) {
    	FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = new FlowPropertyDefinitionBuilder(flowPropertyDefinition)
        	.applyFlowPropertyExpectations(additionalConfigurationParameters)
        	.applyDefaultProviders(flowPropertyProvider, this);
    	FlowPropertyDefinitionImplementor returnedFlowPropertyDefinition = flowPropertyDefinitionBuilder.toFlowPropertyDefinition();
        // TODO : also create a "read-only" v. writeable property mechanism.
        if ( !returnedFlowPropertyDefinition.isCacheOnly()) {
        	// only set persisters on non-cache-only objects.
            FlowPropertyValuePersister<?> flowPropertyValuePersister = returnedFlowPropertyDefinition.getFlowPropertyValuePersister();
            if ( flowPropertyValuePersister instanceof FlowPropertyDefinitionProvider && flowPropertyValuePersister != this){
                // TODO: note: infinite loop possibilities here if 2 different objects have mutually dependent FPDs
                ((FlowPropertyDefinitionProvider)flowPropertyValuePersister).defineFlowPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters);
            }
        }
        return returnedFlowPropertyDefinition;
    }

    /**
     *
     * @param flowPropertyProvider
     */
    public final void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider) {
        this.defineFlowPropertyDefinitions(flowPropertyProvider, null);
    }
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        this.addDefinedPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters );
    }
}
