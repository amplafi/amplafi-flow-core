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

import static com.sworddance.util.CUtilities.*;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowStepDirection;
import org.amplafi.flow.translator.FlowTranslator;
import org.apache.commons.lang.builder.EqualsBuilder;

import com.sworddance.util.NotNullIterator;
/**
 * Used to help configure properties created by {@link FlowPropertyDefinitionProvider}s.
 *
 * @author patmoore
 *
 */
public class FlowPropertyExpectationImpl implements FlowPropertyExpectation {

    /**
     * All properties should be considered "final" - but because for convienece we cannot set everything in ctors,
     * they are not java finals.
     */
    private final List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners;

    private final String name;
    private final FlowActivityPhase propertyRequired;
    private final PropertyScope propertyScope;
    private final PropertyUsage propertyUsage;

    private final FlowPropertyValueProvider<?> flowPropertyValueProvider;
    private final FlowPropertyValuePersister flowPropertyValuePersister;

    private final ExternalPropertyAccessRestriction externalPropertyAccessRestriction;

    private final DataClassDefinition dataClassDefinition;

    private final Set<String> alternates;
    /**
     * on {@link FlowActivity#passivate(boolean, FlowStepDirection)} the object should be saved
     * back. This allows complex object to have their string representation
     * saved. Necessary for cases when setProperty() is not used to save value
     * changes, because the object stored in the property is being modified not
     * the usual case of where a new object is saved.
     *
     * Use case: Any collections. Maybe collection properties should automatically have saveBack set?
     */
    private final Boolean saveBack;

    /**
     * if the property does not exist in the cache then create a new instance.
     * As a result, the property will never return a null.
     */
    private final Boolean autoCreate;

    @Deprecated
    private final String initial;

    // TODO : Map<String(getName(), FPE> so that way 2 properties with same name can be associated and merged.
    // FPE - must have names
    private final Set<FlowPropertyExpectation> propertiesDependentOn;

    public FlowPropertyExpectationImpl() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    /**
     * All properties should have the
     * @param flowActivityPhase
     */
    public FlowPropertyExpectationImpl(FlowActivityPhase flowActivityPhase) {
        this(null, flowActivityPhase, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    /**
     * Used to declare a dependency.
     * TODO: the expected data class?
     * @param name
     */
    public FlowPropertyExpectationImpl(String name) {
        this(name, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    public FlowPropertyExpectationImpl(RegisteredStandardClass registeredStandardClassAnnotation) {
        this(null, registeredStandardClassAnnotation.defaultFlowActivityPhase(), null, registeredStandardClassAnnotation.defaultPropertyUsage(), registeredStandardClassAnnotation.defaultExternalPropertyAccessRestriction());
    }
    public FlowPropertyExpectationImpl(String name, FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        this(name, null, null, null, null, null, null, Arrays.asList(flowPropertyValueChangeListener), null, null, null, null, null, null);
    }
    public FlowPropertyExpectationImpl(String name, PropertyUsage propertyUsage) {
        this(name, null, null, propertyUsage, null, null, null, null, null, null, null, null, null, null);
    }
    /**
     *
     * @param name
     * @param propertyRequired
     * @param propertyScope
     * @param propertyUsage
     * @param externalPropertyAccessRestriction
     */
    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
        ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this(name, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction, null, null, null, null, null, null, null, null, null);
    }
    /**
     *
     * @param propertyRequired
     * @param propertyScope
     * @param propertyUsage
     * @param externalPropertyAccessRestriction
     */
    public FlowPropertyExpectationImpl(FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
        ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this(null, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction, null, null, null, null, null, null, null, null, null);
    }
    /**
     *
     * @param name
     * @param propertyRequired
     * @param propertyScope
     * @param propertyUsage
     * @param externalPropertyAccessRestriction
     * @param flowPropertyValueChangeListener
     */
    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
            ExternalPropertyAccessRestriction externalPropertyAccessRestriction, FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        this(name, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction, null, null, Arrays.asList(flowPropertyValueChangeListener), null, null, null, null, null, null);
    }
    /**
     *
     * @param externalPropertyAccessRestriction
     */
    public FlowPropertyExpectationImpl(ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this(null, null, null, null, externalPropertyAccessRestriction, null, null, null, null, null, null, null, null, null);
    }
    /**
     *
     * @param name
     * @param propertyRequired
     * @param propertyScope
     * @param propertyUsage
     * @param externalPropertyAccessRestriction
     * @param flowPropertyValueProvider
     * @param flowPropertyValuePersister
     * @param flowPropertyValueChangeListeners
     * @param saveBack
     * @param autoCreate
     * @param alternates
     * @param dataClassDefinition
     * @param propertiesDependentOn TODO
     * @param initial TODO
     */
    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
        ExternalPropertyAccessRestriction externalPropertyAccessRestriction,
        FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, FlowPropertyValuePersister flowPropertyValuePersister, List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners,
        Boolean saveBack, Boolean autoCreate, Set<String> alternates, DataClassDefinition dataClassDefinition, Set<FlowPropertyExpectation> propertiesDependentOn, String initial) {
        this.name = name;
        this.propertyRequired = propertyRequired;
        this.propertyScope = propertyScope;
        this.propertyUsage = propertyUsage;
        this.externalPropertyAccessRestriction = externalPropertyAccessRestriction;
        this.flowPropertyValueChangeListeners = isNotEmpty(flowPropertyValueChangeListeners)?Collections.unmodifiableList(new ArrayList<>(flowPropertyValueChangeListeners)):null;
        this.flowPropertyValueProvider = flowPropertyValueProvider;
        this.flowPropertyValuePersister = flowPropertyValuePersister;
        this.saveBack = saveBack;
        // TODO: unmodifiable copy
        this.alternates = alternates;
        this.autoCreate = autoCreate;
        this.dataClassDefinition = dataClassDefinition;
        // TODO: unmodifiable copy
        this.propertiesDependentOn = propertiesDependentOn;
        this.initial = initial;
    }

    /**
     * @param dataClassDefinition the dataClassDefinition to set
     */
    public FlowPropertyExpectationImpl(DataClassDefinition dataClassDefinition) {
        this(null, null,null, null,null, null,null, null,null, null, null, dataClassDefinition, null, null);
    }

    /**
     *
     * @param sourceFlowPropertyExpectation
     * @param defaultFlowPropertyExpectation values used if sourceFlowPropertyExpectation does not have a value
     * the exception are the {@link FlowPropertyValueChangeListener}s which are combined.
     */
    public FlowPropertyExpectationImpl(FlowPropertyExpectation sourceFlowPropertyExpectation, FlowPropertyExpectation defaultFlowPropertyExpectation) {
       this.name = sourceFlowPropertyExpectation.getName() !=  null?
           sourceFlowPropertyExpectation.getName() :defaultFlowPropertyExpectation.getName();
       this.propertyRequired = sourceFlowPropertyExpectation.getPropertyRequired() !=  null?
           sourceFlowPropertyExpectation.getPropertyRequired() :defaultFlowPropertyExpectation.getPropertyRequired();
       this.propertyScope = sourceFlowPropertyExpectation.getPropertyScope() !=  null?
               sourceFlowPropertyExpectation.getPropertyScope() :defaultFlowPropertyExpectation.getPropertyScope();
       this.propertyUsage = sourceFlowPropertyExpectation.getPropertyUsage() !=  null?
           sourceFlowPropertyExpectation.getPropertyUsage() :defaultFlowPropertyExpectation.getPropertyUsage();

       this.flowPropertyValueProvider = sourceFlowPropertyExpectation.getFlowPropertyValueProvider() != null?
           sourceFlowPropertyExpectation.getFlowPropertyValueProvider() :defaultFlowPropertyExpectation.getFlowPropertyValueProvider();
       this.flowPropertyValuePersister = sourceFlowPropertyExpectation.getFlowPropertyValuePersister() != null?
           sourceFlowPropertyExpectation.getFlowPropertyValuePersister() :defaultFlowPropertyExpectation.getFlowPropertyValuePersister();

       this.externalPropertyAccessRestriction= sourceFlowPropertyExpectation.getExternalPropertyAccessRestriction() != null?
           sourceFlowPropertyExpectation.getExternalPropertyAccessRestriction() :defaultFlowPropertyExpectation.getExternalPropertyAccessRestriction();

       this.dataClassDefinition= sourceFlowPropertyExpectation.getDataClassDefinition() !=  null?
           sourceFlowPropertyExpectation.getDataClassDefinition() :defaultFlowPropertyExpectation.getDataClassDefinition();

       // all changelisteners are used
       List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners = new ArrayList<>();
       if ( isNotEmpty(defaultFlowPropertyExpectation.getFlowPropertyValueChangeListeners())) {
           flowPropertyValueChangeListeners.addAll(defaultFlowPropertyExpectation.getFlowPropertyValueChangeListeners());
       }
       if ( isNotEmpty(sourceFlowPropertyExpectation.getFlowPropertyValueChangeListeners())) {
           flowPropertyValueChangeListeners.addAll(sourceFlowPropertyExpectation.getFlowPropertyValueChangeListeners());
       }
       this.flowPropertyValueChangeListeners = Collections.unmodifiableList(flowPropertyValueChangeListeners);

       this.saveBack = getFirstNonNull(sourceFlowPropertyExpectation.getSaveBack(), defaultFlowPropertyExpectation.getSaveBack());
       this.autoCreate = getFirstNonNull(sourceFlowPropertyExpectation.getAutoCreate(), defaultFlowPropertyExpectation.getAutoCreate());
       Set<String> alternates= new HashSet<>();
       addAll(alternates,sourceFlowPropertyExpectation.getAlternates());
       addAll(alternates,defaultFlowPropertyExpectation.getAlternates());
       if ( isNotEmpty(alternates)) {
           this.alternates = Collections.unmodifiableSet(alternates);
       } else {
           this.alternates = null;
       }
       Set<FlowPropertyExpectation> propertyExpectations = new HashSet<>();
       if ( isNotEmpty(sourceFlowPropertyExpectation.getPropertiesDependentOn())) {
           propertyExpectations.addAll(sourceFlowPropertyExpectation.getPropertiesDependentOn());
       }
       if ( isNotEmpty(defaultFlowPropertyExpectation.getPropertiesDependentOn())) {
           propertyExpectations.addAll(defaultFlowPropertyExpectation.getPropertiesDependentOn());
       }
       this.propertiesDependentOn = propertyExpectations.isEmpty()?null: Collections.unmodifiableSet(propertyExpectations);
       this.initial = getFirstNonNull(sourceFlowPropertyExpectation.getInitial(), defaultFlowPropertyExpectation.getInitial());
    }

    /**
     *
     * @param sourceFlowPropertyExpectation
     * @param defaultFlowPropertyExpectation values used if sourceFlowPropertyExpectation does not have a value
     * the exception are the {@link FlowPropertyValueChangeListener}s which are combined.
     */
    public FlowPropertyExpectationImpl(FlowPropertyExpectation sourceFlowPropertyExpectation) {
       this.name =
           sourceFlowPropertyExpectation.getName();
       this.propertyRequired = sourceFlowPropertyExpectation.getPropertyRequired() ;
       this.propertyScope = sourceFlowPropertyExpectation.getPropertyScope();
       this.propertyUsage = sourceFlowPropertyExpectation.getPropertyUsage();

       this.flowPropertyValueProvider = sourceFlowPropertyExpectation.getFlowPropertyValueProvider();
       this.flowPropertyValuePersister = sourceFlowPropertyExpectation.getFlowPropertyValuePersister();

       this.externalPropertyAccessRestriction= sourceFlowPropertyExpectation.getExternalPropertyAccessRestriction();

       this.dataClassDefinition= sourceFlowPropertyExpectation.getDataClassDefinition();

       // all changelisteners are used
       List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners = new ArrayList<>();
       if ( isNotEmpty(sourceFlowPropertyExpectation.getFlowPropertyValueChangeListeners())) {
           flowPropertyValueChangeListeners.addAll(sourceFlowPropertyExpectation.getFlowPropertyValueChangeListeners());
       }
       this.flowPropertyValueChangeListeners = Collections.unmodifiableList(flowPropertyValueChangeListeners);

       this.saveBack = sourceFlowPropertyExpectation.getSaveBack();
       this.autoCreate = sourceFlowPropertyExpectation.getAutoCreate();
       Set<String> alternates= new HashSet<>();
       addAll(alternates,sourceFlowPropertyExpectation.getAlternates());
       if ( isNotEmpty(alternates.isEmpty())) {
           this.alternates = Collections.unmodifiableSet(alternates);
       } else {
           this.alternates = null;
       }
       Set<FlowPropertyExpectation> propertyExpectations = new HashSet<>();
       if ( isNotEmpty(sourceFlowPropertyExpectation.getPropertiesDependentOn())) {
           propertyExpectations.addAll(sourceFlowPropertyExpectation.getPropertiesDependentOn());
       }
       this.propertiesDependentOn = propertyExpectations.isEmpty()?null: Collections.unmodifiableSet(propertyExpectations);
       this.initial = sourceFlowPropertyExpectation.getInitial();
    }

    /**
     * @see org.amplafi.flow.FlowPropertyExpectation#merge(org.amplafi.flow.FlowPropertyExpectation)
     */
    @Override
    public FlowPropertyExpectationImpl merge(FlowPropertyExpectation flowPropertyExpectation) {
        if ( this.isApplicable(flowPropertyExpectation)) {
            return new FlowPropertyExpectationImpl(flowPropertyExpectation, this);
        } else {
            return this;
        }
    }
    public FlowPropertyExpectationImpl merge(Iterable<FlowPropertyExpectation> flowPropertyExpectations) {
        FlowPropertyExpectationImpl result = this;
        for(FlowPropertyExpectation flowPropertyExpectation : NotNullIterator.<FlowPropertyExpectation>newNotNullIterator(flowPropertyExpectations)) {
            result = result.merge(flowPropertyExpectation);
        }
        return result;
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
    public boolean isAssignableFrom(Class<?> clazz) {
        return this.getDataClassDefinition().isAssignableFrom(clazz);
    }

    @Override
    public FlowPropertyValuePersister getFlowPropertyValuePersister() {
        return flowPropertyValuePersister;
    }
    @Override
    public ExternalPropertyAccessRestriction getExternalPropertyAccessRestriction() {
        return externalPropertyAccessRestriction;
    }
    public FlowTranslator<?> getTranslator() {
        return this.getDataClassDefinition().getFlowTranslator();
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
    @Override
    public Boolean getSaveBack() {
        return saveBack;
    }
    @Override
    public Set<String> getAlternates() {
        return alternates;
    }
    @Override
    public Boolean getAutoCreate() {
        return autoCreate;
    }
    /**
     * @return the propertiesDependentOn
     */
    @Override
    public Set<FlowPropertyExpectation> getPropertiesDependentOn() {
        return this.propertiesDependentOn;
    }
    @Override
    public String getInitial() {
        return initial;
    }
    @Override
    public boolean equals(Object o) {
        if ( !(o instanceof FlowPropertyExpectation)) {
            return false;
        } else {
            FlowPropertyExpectation other = (FlowPropertyExpectation) o;
            return new EqualsBuilder()
                .append(this.alternates, other.getAlternates())
                .append(this.autoCreate, other.getAutoCreate())
                .append(this.dataClassDefinition, other.getDataClassDefinition())
                .append(this.externalPropertyAccessRestriction, other.getExternalPropertyAccessRestriction())
                .append(this.flowPropertyValueChangeListeners, other.getFlowPropertyValueChangeListeners())
                .append(this.flowPropertyValuePersister, other.getFlowPropertyValuePersister())
                .append(this.flowPropertyValueProvider, other.getFlowPropertyValueProvider())
                .append(this.initial, this.getInitial())
                .append(this.name, this.getName())
                .append(this.propertiesDependentOn, other.getPropertiesDependentOn())
                .append(this.propertyRequired, other.getPropertyRequired())
                .append(this.propertyScope, other.getPropertyScope())
                .append(this.propertyUsage, other.getPropertyUsage())
                .append(this.saveBack, other.getSaveBack())
                .isEquals();
        }
    }
}
