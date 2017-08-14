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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.FlowValidationResultProvider;

import static com.sworddance.util.CUtilities.*;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.NotNullIterator;

/**
 * support methods for {@link FlowPropertyDefinitionBuilderProvider} implementations.
 * Since this class does not implement any methods in {@link FlowPropertyDefinitionBuilderProvider} this class does not implement {@link FlowPropertyDefinitionBuilderProvider}.
 * @author patmoore
 *
 */
public class BaseFlowPropertyDefinitionBuilderProvider {
    // Forcing a fixed order so getting the default first FPD will be consistent (TODO save first FPD explicitly)
    private final LinkedHashMap<String, FlowPropertyDefinitionBuilder> flowPropertyDefinitions = new LinkedHashMap<>();

    /**
     * Additional flowPropertyDefinitionBuilders can be added with {@link #addFlowPropertyDefinitionImplementators(FlowPropertyDefinitionBuilder...)} )
     * @param flowPropertyDefinitionBuilders can be null or missing.
     */
    protected BaseFlowPropertyDefinitionBuilderProvider(FlowPropertyDefinitionBuilder...flowPropertyDefinitionBuilders) {
        addFlowPropertyDefinitionImplementators(flowPropertyDefinitionBuilders);
    }

    protected void addFlowPropertyDefinitionImplementators(FlowPropertyDefinitionBuilder... flowPropertyDefinitionBuilders) {
        for(FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder: NotNullIterator.<FlowPropertyDefinitionBuilder>newNotNullIterator(flowPropertyDefinitionBuilders)) {
            if ( flowPropertyDefinitionBuilder.getName() == null ) {
                throw new FlowConfigurationException("Only definitions with names can be provided "+flowPropertyDefinitionBuilder);
            }
            this.flowPropertyDefinitions.put(flowPropertyDefinitionBuilder.getName(), flowPropertyDefinitionBuilder);
            // Apply default of the defining FlowPropertyValueProvider.
            flowPropertyDefinitionBuilder.applyDefaultProviders(this);
        }
    }

    protected Map<String, FlowPropertyDefinitionBuilder> getFlowPropertyDefinitions() {
        return this.flowPropertyDefinitions;
    }

    protected FlowPropertyDefinitionBuilder getFlowPropertyDefinitionBuilder(String name) {
        return getFlowPropertyDefinitions().get(name);
    }

    public Set<String> getFlowPropertyDefinitionNames() {
        return getFlowPropertyDefinitions().keySet();
    }
    public List<String> getOutputFlowPropertyDefinitionNames() {
        List<String> outputFlowPropertyDefinitionNames = new ArrayList<>();
        for(FlowPropertyDefinitionBuilder flowPropertyDefinition : this.getFlowPropertyDefinitions().values()) {
            if ( flowPropertyDefinition.isOutputedProperty()) {
                outputFlowPropertyDefinitionNames.add(flowPropertyDefinition.getName());
            }
        }
        if ( isEmpty(outputFlowPropertyDefinitionNames)) {
            FlowPropertyDefinitionBuilder byDefaultFirst = get(this.getFlowPropertyDefinitions().values(),0);
            outputFlowPropertyDefinitionNames.add(byDefaultFirst.getName());
        }
        return outputFlowPropertyDefinitionNames;
    }

    /**
     * Add additional {@link FlowPropertyDefinition}s to the {@link FlowPropertyProviderImplementor}. This is used when
     * a {@link FlowPropertyValueProvider} wishes to define a property that the {@link FlowPropertyValueProvider} does not supply
     * a value for.
     *
     * @param flowPropertyProvider
     * @param flowPropertyDefinitionBuilders
     * @param additionalConfigurationParameters most of the time this is null because expectations applied to the master definition usually do not apply to the
     * dependent properties.
     */
    protected void addFlowPropertyExpectations(FlowPropertyProviderImplementor flowPropertyProvider,Collection<FlowPropertyDefinitionBuilder>flowPropertyDefinitionBuilders, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        for(FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder: flowPropertyDefinitionBuilders) {
            addFlowPropertyExpectation(flowPropertyProvider, flowPropertyDefinitionBuilder, additionalConfigurationParameters);
        }
    }
    protected void addFlowPropertyExpectation(FlowPropertyProviderImplementor flowPropertyProvider,
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        FlowPropertyDefinitionImplementor returnedFlowPropertyDefinition = initPropertyDefinition(flowPropertyProvider, flowPropertyDefinitionBuilder, additionalConfigurationParameters);
        flowPropertyProvider.addPropertyDefinitions(returnedFlowPropertyDefinition);
    }

    public FlowPropertyDefinitionBuilder getFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass) {
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = this.getFlowPropertyDefinitionBuilder(propertyName);
        if ( flowPropertyDefinitionBuilder == null || (dataClass != null && !flowPropertyDefinitionBuilder.isAssignableFrom(dataClass))) {
            return null;
        } else {
            return new FlowPropertyDefinitionBuilder(flowPropertyDefinitionBuilder);
        }
    }
    /**
     * TODO: put in the {@link FlowPropertyDefinitionBuilder} code so that universally available for all properties.
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
            if ( flowPropertyValuePersister instanceof FlowPropertyDefinitionBuilderProvider && flowPropertyValuePersister != this){
                // TODO: note: infinite loop possibilities here if 2 different objects have mutually dependent FPDs
                ((FlowPropertyDefinitionBuilderProvider)flowPropertyValuePersister).defineFlowPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters);
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
     * add ALL the {@link FlowPropertyDefinition}s provided by this definition provider to flowPropertyProvider.
     * in the constructor or {@link #addFlowPropertyDefinitionImplementators(FlowPropertyDefinitionBuilder...)}
     * methods.
     *
     * @param flowPropertyProvider
     * @param additionalConfigurationParameters - a list because we want consistent fixed order that the additionalConfigurationParameters are applied.
     */
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
        if ( this.getFlowPropertyDefinitions() != null) {
            for(FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder: this.getFlowPropertyDefinitions().values()) {
                addFlowPropertyExpectation(flowPropertyProvider, new FlowPropertyDefinitionBuilder(flowPropertyDefinitionBuilder), additionalConfigurationParameters);
            }
        }
    }

    /**
     * This is a hack: but right now I am not certain we want to this in a global way for all FPDPs
     * right now it is called from subclass's defineFlowPropertyDefinitions()
     */
    protected void handleFlowValidationResultProvider(FlowPropertyProviderImplementor flowPropertyProvider) {
        // HACK - we need ability to provide this for FlowStateImpl and to use interfaces.
        if ( flowPropertyProvider instanceof FlowActivityImpl && this instanceof FlowValidationResultProvider) {
            ((FlowActivityImpl)flowPropertyProvider).addFlowValidationResultProvider((FlowValidationResultProvider<FlowPropertyProviderImplementor>)this);
        }
    }
    /**
     * convenient default method if subclass implements FlowPropertyValuePersister
     * and the persister needs the value ( which is most of the time)
     */
    protected Object saveChanges(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, Object currentValue) {
        throw new UnsupportedOperationException("no method defined");
    }

    /**
     * Most subclasses should override {@link #saveChanges(FlowPropertyProviderWithValues, FlowPropertyDefinition, Object)}
     * However in a few cases we don't want to trigger property retrieval as part of the save ( we want to see if the value is set at all )
     */
    public Object saveChanges(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        Object property = flowPropertyProvider.getPropertyWithDefinition(flowPropertyDefinition);
        return saveChanges(flowPropertyProvider, flowPropertyDefinition, property);
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
        if ( flowPropertyDefinition != null) {
            ApplicationIllegalArgumentException.valid(!flowPropertyDefinition.isNamed(propertyName), propertyName);
        }
        T result = flowPropertyProvider.getProperty(propertyName, expected);
        FlowValidationException.notNull(result, flowPropertyProvider, propertyName, messages);
        return result;
    }
    protected <T> T getRequired(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, Class<? extends T> propertyClass, Object...messages) {
        if ( flowPropertyDefinition != null) {
            ApplicationIllegalArgumentException.valid(!flowPropertyDefinition.isNamed(propertyClass), propertyClass);
        }
        T result = flowPropertyProvider.getProperty(propertyClass);
        FlowValidationException.notNull(result, flowPropertyProvider, propertyClass, messages);
        return result;
    }
}
