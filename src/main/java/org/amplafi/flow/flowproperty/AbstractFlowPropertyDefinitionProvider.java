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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;

import static com.sworddance.util.CUtilities.*;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationNullPointerException;
import com.sworddance.util.NotNullIterator;

/**
 * support methods for {@link FlowPropertyDefinitionProvider} implementations.
 * Since this class does not implement any methods in {@link FlowPropertyDefinitionProvider} this class does not implement {@link FlowPropertyDefinitionProvider}.
 * @author patmoore
 *
 */
public abstract class AbstractFlowPropertyDefinitionProvider {
    private final Map<String, FlowPropertyDefinitionImplementor> flowPropertyDefinitions = new ConcurrentHashMap<String, FlowPropertyDefinitionImplementor>();

    protected AbstractFlowPropertyDefinitionProvider() {
        // for case when definitions are added in later.
    }
    protected AbstractFlowPropertyDefinitionProvider(FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        addFlowPropertyDefinitionImplementators(flowPropertyDefinitions);
    }
    protected AbstractFlowPropertyDefinitionProvider(FlowPropertyDefinitionBuilder...flowPropertyDefinitionBuilders) {
        addFlowPropertyDefinitionImplementators(flowPropertyDefinitionBuilders);
    }

    public void addFlowPropertyDefinitionImplementators(FlowPropertyDefinitionBuilder... flowPropertyDefinitionBuilders) {
        for(FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder: NotNullIterator.<FlowPropertyDefinitionBuilder>newNotNullIterator(flowPropertyDefinitionBuilders)) {
            FlowPropertyDefinitionImplementor outputed = flowPropertyDefinitionBuilder.toFlowPropertyDefinition();
            outputed.setTemplateFlowPropertyDefinition();
            put(this.flowPropertyDefinitions, outputed);
            // Apply default of the defining FlowPropertyValueProvider.
            flowPropertyDefinitionBuilder.applyDefaultProviders(this);
        }
    }
    public void addFlowPropertyDefinitionImplementators(FlowPropertyDefinitionImplementor... flowPropertyDefinitionImplementors) {
        for(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor: NotNullIterator.<FlowPropertyDefinitionImplementor>newNotNullIterator(flowPropertyDefinitionImplementors)) {
            FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = new FlowPropertyDefinitionBuilder().createFromTemplate(flowPropertyDefinitionImplementor);
            addFlowPropertyDefinitionImplementators(flowPropertyDefinitionBuilder);
        }
    }

    protected Map<String, FlowPropertyDefinitionImplementor> getFlowPropertyDefinitions() {
        return this.flowPropertyDefinitions;
    }

    public Set<String> getFlowPropertyDefinitionNames() {
        return getFlowPropertyDefinitions().keySet();
    }
    public List<String> getOutputFlowPropertyDefinitionNames() {
        List<String> outputFlowPropertyDefinitionNames = new ArrayList<String>();
        for(FlowPropertyDefinition flowPropertyDefinition : this.flowPropertyDefinitions.values()) {
            if ( flowPropertyDefinition.getPropertyUsage().isOutputedProperty()) {
                outputFlowPropertyDefinitionNames.add(flowPropertyDefinition.getName());
            }
        }
        return outputFlowPropertyDefinitionNames;
    }
    /**
     * add ALL the {@link FlowPropertyDefinition}s provided by this definition provider to flowPropertyProvider.
     * @param flowPropertyProvider
     * @param additionalConfigurationParameters a list because order is significant
     */
    protected void addDefinedPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        if ( this.flowPropertyDefinitions != null) {
            List<FlowPropertyDefinitionImplementor> clonedFlowPropertyDefinitions = new ArrayList<FlowPropertyDefinitionImplementor>();
            for(FlowPropertyDefinitionImplementor flowPropertyDefinition: this.flowPropertyDefinitions.values()) {
                clonedFlowPropertyDefinitions.add(flowPropertyDefinition);
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
        for(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor: flowPropertyDefinitions) {
            addPropertyDefinition(flowPropertyProvider, flowPropertyDefinitionImplementor, additionalConfigurationParameters);
        }
    }
    protected void addPropertyDefinition(FlowPropertyProviderImplementor flowPropertyProvider,
        FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = new FlowPropertyDefinitionBuilder().createFromTemplate(flowPropertyDefinitionImplementor);
        addPropertyDefinition(flowPropertyProvider, flowPropertyDefinitionBuilder, additionalConfigurationParameters);
    }
    protected void addPropertyDefinition(FlowPropertyProviderImplementor flowPropertyProvider,
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        FlowPropertyDefinitionImplementor returnedFlowPropertyDefinition = initPropertyDefinition(flowPropertyProvider, flowPropertyDefinitionBuilder, additionalConfigurationParameters);
        flowPropertyProvider.addPropertyDefinitions(returnedFlowPropertyDefinition);
    }

    public FlowPropertyDefinitionBuilder getFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass) {
        FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor = this.flowPropertyDefinitions.get(propertyName);
        if ( flowPropertyDefinitionImplementor == null || (dataClass != null && !flowPropertyDefinitionImplementor.isAssignableFrom(dataClass))) {
            return null;
        } else {
            return new FlowPropertyDefinitionBuilder().createFromTemplate(flowPropertyDefinitionImplementor);
        }
    }
    /**
     * initialize a flowPropertyDefinition.
     * @param flowPropertyProvider
     * @param flowPropertyDefinitionBuilder will be modified (make sure not modifying the master definition)
     * @param additionalConfigurationParameters
     */
    protected FlowPropertyDefinitionImplementor initPropertyDefinition(
        FlowPropertyProviderImplementor flowPropertyProvider,
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        flowPropertyDefinitionBuilder = flowPropertyDefinitionBuilder
        	.applyFlowPropertyExpectations(additionalConfigurationParameters)
        	.applyDefaultProviders(flowPropertyProvider, this);
    	FlowPropertyDefinitionImplementor returnedFlowPropertyDefinition = flowPropertyDefinitionBuilder.toFlowPropertyDefinition();
        // TODO : also create a "read-only" v. writeable property mechanism
    	// TODO feels like it should be part of an 'FlowPropertyDefinitionBuilder.apply()' method
        if ( !returnedFlowPropertyDefinition.isReadOnly()) {
        	// only set persisters on non-read-only objects.
            FlowPropertyValuePersister<?> flowPropertyValuePersister = returnedFlowPropertyDefinition.getFlowPropertyValuePersister();
            if ( flowPropertyValuePersister instanceof FlowPropertyDefinitionProvider && flowPropertyValuePersister != this){
                // TODO: note: infinite loop possibilities here if 2 different objects have mutually dependent FPDs
                ((FlowPropertyDefinitionProvider)flowPropertyValuePersister).defineFlowPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters);
            }
        }
        return returnedFlowPropertyDefinition;
    }

    /**
     * called in {@link org.amplafi.flow.impl.FlowActivityImpl#addStandardFlowPropertyDefinitions} or a {@link org.amplafi.flow.impl.FlowActivityImpl} subclass's method.
     * @param flowPropertyProvider
     */
    public final void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider) {
        this.defineFlowPropertyDefinitions(flowPropertyProvider, null);
    }

    /**
     * @param flowPropertyProvider
     * @param additionalConfigurationParameters - a list because we want consistent fixed order that the additionalConfigurationParameters are applied.
     */
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        this.addDefinedPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters );
    }

    // -------------------------
    // convenient default method if subclass implements FlowPropertyValuePersister
    protected void saveChanges(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, Object currentValue) {
        throw new UnsupportedOperationException("no method defined");
    }
    public void saveChanges(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        Object property = flowPropertyProvider.getProperty(flowPropertyDefinition.getName());
        saveChanges(flowPropertyProvider, flowPropertyDefinition, property);
    }
    // ------------------------
    @SuppressWarnings("unchecked")
    protected <T> T getProperty(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
        return (T) getProperty(flowPropertyProvider, flowPropertyDefinition, propertyName, null);
    }
    protected <T> T getProperty(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, Class<T> propertyClass) {
        return getProperty(flowPropertyProvider, flowPropertyDefinition, null, propertyClass);
    }
    protected <T> T getProperty(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName, Class<? extends T> propertyClass) {
        if ( propertyName != null ) {
            return flowPropertyProvider.getProperty(propertyName, propertyClass);
        } else if ( propertyClass != null ) {
            return flowPropertyProvider.getProperty(propertyClass);
        }
        // TODO throw exception?
        return null;
    }
    /**
     *
     * @param <T>
     * @param flowPropertyProvider -- should this be FPP?
     * @param flowPropertyDefinition
     * @param propertyName
     * @return will not be null.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getRequired(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName, Object...messages) {
        return (T) this.getRequired(flowPropertyProvider, flowPropertyDefinition, propertyName, null, messages);
    }
    protected <T> T getRequired(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName, Class<? extends T> expected, Object...messages) {
        ApplicationIllegalArgumentException.valid(!flowPropertyDefinition.isNamed(propertyName), propertyName);
        T result = flowPropertyProvider.getProperty(propertyName, expected);
        ApplicationNullPointerException.notNull(result, propertyName, messages);
        return result;
    }
    protected <T> T getRequired(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, Class<? extends T> propertyClass, Object...messages) {
        ApplicationIllegalArgumentException.valid(!flowPropertyDefinition.isNamed(propertyClass), propertyClass);
        T result = flowPropertyProvider.getProperty(propertyClass);
        ApplicationNullPointerException.notNull(result, propertyClass, messages);
        return result;
    }
}
