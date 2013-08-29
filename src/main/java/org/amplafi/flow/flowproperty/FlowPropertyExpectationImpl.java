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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import static com.sworddance.util.CUtilities.*;
import static org.apache.commons.lang.StringUtils.isBlank;
/**
 * Used to help configure properties created by {@link FlowPropertyDefinitionProvider}s.
 *
 * @author patmoore
 *
 */
public class FlowPropertyExpectationImpl implements FlowPropertyExpectation {

    private final List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners;

    private final String name;
    private final FlowActivityPhase propertyRequired;
    private final PropertyScope propertyScope;
    private final PropertyUsage propertyUsage;

    private final FlowPropertyValueProvider<?> flowPropertyValueProvider;
    private final FlowPropertyValuePersister flowPropertyValuePersister;

    private final ExternalPropertyAccessRestriction externalPropertyAccessRestriction;

    private DataClassDefinition dataClassDefinition;

    public FlowPropertyExpectationImpl() {
        this(null, null, null, null, null, null, null, null);
    }
    /**
     * All properties should have the
     * @param flowActivityPhase
     */
    public FlowPropertyExpectationImpl(FlowActivityPhase flowActivityPhase) {
        this(null, flowActivityPhase, null, null, null, null, null, null);
    }
    /**
     * Used to declare a dependency.
     * TODO: the expected data class?
     * @param name
     */
    public FlowPropertyExpectationImpl(String name) {
        this(name, null, null, null, null, null, null, null);
    }
    public FlowPropertyExpectationImpl(RegisteredStandardClass registeredStandardClassAnnotation) {
        this(null, registeredStandardClassAnnotation.defaultFlowActivityPhase(), null, registeredStandardClassAnnotation.defaultPropertyUsage(), registeredStandardClassAnnotation.defaultExternalPropertyAccessRestriction());
    }
    public FlowPropertyExpectationImpl(String name, FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        this(name, null, null, null, null, null, null, Arrays.asList(flowPropertyValueChangeListener));
    }
    public FlowPropertyExpectationImpl(String name, FlowPropertyValueProvider flowPropertyValueProvider) {
        this(name, null, null, null, null, flowPropertyValueProvider, null, null);
    }
    public FlowPropertyExpectationImpl(String name, PropertyUsage propertyUsage) {
        this(name, null, null, propertyUsage, null, null, null, null);
    }
    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
        ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this(name, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction, null, null, null);
    }
    public FlowPropertyExpectationImpl(FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
        ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this(null, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction, null, null, null);
    }
    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
            ExternalPropertyAccessRestriction externalPropertyAccessRestriction, FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        this(name, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction, null, null, Arrays.asList(flowPropertyValueChangeListener));
    }

    public FlowPropertyExpectationImpl(FlowPropertyExpectation sourceFlowPropertyExpectation, FlowPropertyExpectation defaultFlowPropertyExpectation) {
       this.name = sourceFlowPropertyExpectation.getName() !=  null?
           sourceFlowPropertyExpectation.getName() :defaultFlowPropertyExpectation.getName();
       this.propertyRequired = sourceFlowPropertyExpectation.getPropertyRequired() !=  null?
           sourceFlowPropertyExpectation.getPropertyRequired() :defaultFlowPropertyExpectation.getPropertyRequired();
       this.propertyScope = sourceFlowPropertyExpectation.getPropertyScope() !=  null?
               sourceFlowPropertyExpectation.getPropertyScope() :defaultFlowPropertyExpectation.getPropertyScope();
       this.propertyUsage = sourceFlowPropertyExpectation.getPropertyUsage() !=  null?
           sourceFlowPropertyExpectation.getPropertyUsage() :defaultFlowPropertyExpectation.getPropertyUsage();
       this.flowPropertyValueProvider = sourceFlowPropertyExpectation.getFlowPropertyValueProvider() !=  null?
           sourceFlowPropertyExpectation.getFlowPropertyValueProvider() :defaultFlowPropertyExpectation.getFlowPropertyValueProvider();
       this.flowPropertyValuePersister = sourceFlowPropertyExpectation.getFlowPropertyValuePersister() !=  null?
           sourceFlowPropertyExpectation.getFlowPropertyValuePersister() :defaultFlowPropertyExpectation.getFlowPropertyValuePersister();

       this.externalPropertyAccessRestriction= sourceFlowPropertyExpectation.getExternalPropertyAccessRestriction() !=  null?
           sourceFlowPropertyExpectation.getExternalPropertyAccessRestriction() :defaultFlowPropertyExpectation.getExternalPropertyAccessRestriction();

       this.dataClassDefinition= sourceFlowPropertyExpectation.getDataClassDefinition() !=  null?
           sourceFlowPropertyExpectation.getDataClassDefinition() :defaultFlowPropertyExpectation.getDataClassDefinition();

       List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners = new ArrayList<>();

       if ( isNotEmpty(defaultFlowPropertyExpectation.getFlowPropertyValueChangeListeners())) {
           flowPropertyValueChangeListeners.addAll(defaultFlowPropertyExpectation.getFlowPropertyValueChangeListeners());
       }
       if ( isNotEmpty(sourceFlowPropertyExpectation.getFlowPropertyValueChangeListeners())) {
           flowPropertyValueChangeListeners.addAll(sourceFlowPropertyExpectation.getFlowPropertyValueChangeListeners());
       }
       this.flowPropertyValueChangeListeners = Collections.unmodifiableList(flowPropertyValueChangeListeners);
    }

    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
            ExternalPropertyAccessRestriction externalPropertyAccessRestriction,
            FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, FlowPropertyValuePersister flowPropertyValuePersister, List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners) {
        this.name = name;
        this.propertyRequired = propertyRequired;
        this.propertyScope = propertyScope;
        this.propertyUsage = propertyUsage;
        this.externalPropertyAccessRestriction = externalPropertyAccessRestriction;
        this.flowPropertyValueChangeListeners = isNotEmpty(flowPropertyValueChangeListeners)?Collections.unmodifiableList(new ArrayList<>(flowPropertyValueChangeListeners)):null;
        this.flowPropertyValueProvider = flowPropertyValueProvider;
        this.flowPropertyValuePersister = flowPropertyValuePersister;
    }

    public static FlowPropertyExpectationImpl createDefaultExpectation(String name, Object defaultObject) {
        return new FlowPropertyExpectationImpl(name, new FixedFlowPropertyValueProvider(defaultObject));
    }

    @Override
    public FlowPropertyExpectationImpl merge(FlowPropertyExpectation flowPropertyExpectation) {
        if ( this.isApplicable(flowPropertyExpectation)) {
            return new FlowPropertyExpectationImpl(flowPropertyExpectation, this);
        } else {
            return this;
        }
    }

    /**
     * @return the flowPropertyValueChangeListeners
     */
    @Override
    public List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners() {
        return flowPropertyValueChangeListeners;
    }
    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isNamed(String possibleName) {
        if (isBlank(getName())) {
            return false;
        } else {
            return getName().equals(possibleName);
        }
    }

    @Override
    public String getMapKey() {
        return getName();
    }
    /**
     * @return the propertyRequired
     */
    @Override
    public FlowActivityPhase getPropertyRequired() {
        return propertyRequired;
    }
    /**
     * @return the propertyScope
     */
    @Override
    public PropertyScope getPropertyScope() {
        return propertyScope;
    }
    /**
     * @return the propertyUsage
     */
    @Override
    public PropertyUsage getPropertyUsage() {
        return propertyUsage;
    }
    @Override
    @SuppressWarnings("unchecked")
    public <FA extends FlowPropertyProvider> FlowPropertyValueProvider<FA> getFlowPropertyValueProvider() {
        return (FlowPropertyValueProvider<FA>)flowPropertyValueProvider;
    }

    @Override
    public boolean isApplicable(FlowPropertyExpectation flowPropertyExpectation) {
        return getName() == null || flowPropertyExpectation.isNamed(getName());
    }
    @Override
    public FlowPropertyValuePersister getFlowPropertyValuePersister() {
        return flowPropertyValuePersister;
    }
    @Override
    public ExternalPropertyAccessRestriction getExternalPropertyAccessRestriction() {
        return externalPropertyAccessRestriction;
    }

    /**
     * @param dataClassDefinition the dataClassDefinition to set
     */
    public void setDataClassDefinition(DataClassDefinition dataClassDefinition) {
        this.dataClassDefinition = dataClassDefinition;
    }

    /**
     * @return the dataClassDefinition
     */
    @Override
    public DataClassDefinition getDataClassDefinition() {
        return dataClassDefinition;
    }

    @Override
    public Class<? extends Object> getDataClass() {
        return getDataClassDefinition().getDataClass();
    }
}
