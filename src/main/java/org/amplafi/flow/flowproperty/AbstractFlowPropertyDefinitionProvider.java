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
import java.util.Collections;
import java.util.List;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;

import com.sworddance.util.NotNullIterator;

import static com.sworddance.util.CUtilities.*;

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
        if ( isNotEmpty(flowPropertyDefinitions)) {
            Collections.addAll(this.flowPropertyDefinitions, flowPropertyDefinitions);
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
    @SuppressWarnings({"hiding"})
    protected void addPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider,Collection<FlowPropertyDefinitionImplementor>flowPropertyDefinitions, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        for(FlowPropertyDefinitionImplementor flowPropertyDefinition: flowPropertyDefinitions) {
            initPropertyDefinition(flowPropertyProvider, flowPropertyDefinition, additionalConfigurationParameters);
            flowPropertyProvider.addPropertyDefinitions(flowPropertyDefinition);
        }
    }
    /**
     * initialize a flowPropertyDefinition.
     * @param flowPropertyProvider
     * @param flowPropertyDefinition will be modified (make sure not modifying the master definition)
     * @param additionalConfigurationParameters
     */
    @SuppressWarnings({"unchecked"})
    protected void initPropertyDefinition(
        FlowPropertyProviderImplementor flowPropertyProvider,
        FlowPropertyDefinitionImplementor flowPropertyDefinition, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        applyFlowPropertyExpectations(flowPropertyDefinition, additionalConfigurationParameters);
        if ( !flowPropertyDefinition.isDefaultAvailable() && this instanceof FlowPropertyValueProvider) {
            flowPropertyDefinition.initFlowPropertyValueProvider((FlowPropertyValueProvider)this);
        }
        // TODO : also create a "read-only" v. writeable property mechanism.
        if ( !flowPropertyDefinition.isCacheOnly()) {
            FlowPropertyValuePersister<?> flowPropertyValuePersister = flowPropertyDefinition.getFlowPropertyValuePersister();
            if ( flowPropertyValuePersister == null) {
                if ( flowPropertyProvider instanceof FlowPropertyValuePersister) {
                    flowPropertyValuePersister = (FlowPropertyValuePersister<?>) flowPropertyProvider;
                } else if ( this instanceof FlowPropertyValuePersister) {
                    flowPropertyValuePersister = (FlowPropertyValuePersister<?>) this;
                }
                flowPropertyDefinition.initFlowPropertyValuePersister(flowPropertyValuePersister);
            } else if ( flowPropertyValuePersister instanceof FlowPropertyDefinitionProvider && flowPropertyValuePersister != this){
                // TODO: note: infinite loop possibilities here if 2 different objects have mutually dependent FPDs
                ((FlowPropertyDefinitionProvider)flowPropertyValuePersister).defineFlowPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters);
            }
        }
        if ( this instanceof FlowPropertyValueChangeListener ) {
            flowPropertyDefinition.initFlowPropertyValueChangeListener((FlowPropertyValueChangeListener)this);
        }
    }
    /**
     * scans through all the {@link FlowPropertyExpectation}s looking for expectations that {@link FlowPropertyExpectation#isApplicable(FlowPropertyDefinitionImplementor)}
     * those expectations have their values applied to flowPropertyDefinition in the order they are encountered.
     * @param flowPropertyDefinition
     * @param additionalConfigurationParameters a list because order matters.
     */
    private void applyFlowPropertyExpectations(FlowPropertyDefinitionImplementor flowPropertyDefinition,
            List<FlowPropertyExpectation> additionalConfigurationParameters) {
        for(FlowPropertyExpectation flowPropertyExpectation: NotNullIterator.<FlowPropertyExpectation>newNotNullIterator(additionalConfigurationParameters)) {
            if(flowPropertyExpectation.isApplicable(flowPropertyDefinition)) {
                // FlowPropertyExpectation expectation applies
                flowPropertyDefinition.addFlowPropertyValueChangeListeners(flowPropertyExpectation.getFlowPropertyValueChangeListeners());
                FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider = flowPropertyExpectation.getFlowPropertyValueProvider();
                if ( flowPropertyValueProvider != null) {
                    flowPropertyDefinition.initFlowPropertyValueProvider(flowPropertyValueProvider);
                }
                FlowPropertyValuePersister flowPropertyValuePersister = flowPropertyExpectation.getFlowPropertyValuePersister();
                if ( flowPropertyValuePersister != null) {
                    flowPropertyDefinition.initFlowPropertyValuePersister(flowPropertyValuePersister);
                }
            }
        }
    }
    protected List<? extends FlowPropertyExpectation> getAdditionalConfigParameter(Collection<? extends FlowPropertyExpectation> additionalConfigurationParameters, String key) {
        List<FlowPropertyExpectation> result = new ArrayList<FlowPropertyExpectation>();
        for(FlowPropertyExpectation flowPropertyExpectation: NotNullIterator.<FlowPropertyExpectation>newNotNullIterator(additionalConfigurationParameters)) {
            if ( key.equals(flowPropertyExpectation.getName())) {
                result.add(flowPropertyExpectation);
            }
        }
        return result;
    }

    /**
     *
     * @param flowPropertyProvider
     */
    public final void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider) {
        this.defineFlowPropertyDefinitions(flowPropertyProvider, null);
    }
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        this.addDefinedPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters );
    }
}
