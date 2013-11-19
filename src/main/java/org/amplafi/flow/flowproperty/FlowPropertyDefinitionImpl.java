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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.sworddance.util.CUtilities.*;

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowExecutionException;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowStepDirection;
import org.amplafi.flow.translator.FlowTranslator;

import com.sworddance.util.NotNullIterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import static org.apache.commons.lang.StringUtils.*;

/**
 * Defines a property that will be assigned as part of a {@link Flow} or
 * {@link FlowActivity}. This allows the value to be available to the component
 * or page referenced by a {@link FlowActivity}.
 *
 * @author Patrick Moore
 */
public class FlowPropertyDefinitionImpl implements FlowPropertyDefinitionImplementor {

    /**
     * Name of the property as used in the flow code.
     */
    private String name;

    /**
     * when a flow starts this should be the initial value set unless there is
     * already a value.
     * This is different than the idea of "defaultObject" because the 'initial' check-and-set only happens when the flow is initializing.
     * whereas the 'defaultObject" happens every time there is null for a value.
     *
     * Initial is stored in the FlowState map and defaultObject is not.
     */
    private String initial;

    // TODO: if a list of providers is defined then it becomes impossible to determine the minimum set of dependencies.
    // dependencies required by alternative FPVP may never be needed because the FPVP is never called.
    private transient FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider;
    private transient FlowPropertyValuePersister<FlowPropertyProvider> flowPropertyValuePersister;
    private transient List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners = new CopyOnWriteArrayList<FlowPropertyValueChangeListener>();

    /**
     * on {@link FlowActivity#passivate(boolean, FlowStepDirection)} the object should be saved
     * back. This allows complex object to have their string representation
     * saved. Necessary for cases when setProperty() is not used to save value
     * changes, because the object stored in the property is being modified not
     * the usual case of where a new object is saved.
     *
     * Use case: Any collections. Maybe collection properties should automatically have saveBack set?
     */
    private Boolean saveBack;

    /**
     * if the property does not exist in the cache then create a new instance.
     * As a result, the property will never return a null.
     */
    private Boolean autoCreate;

    private DataClassDefinition dataClassDefinition;

    private Set<String> alternates;

    /**
     * data that should not be outputted or saved. for example, passwords.
     */
    private ExternalPropertyAccessRestriction externalPropertyAccessRestriction;

    private FlowActivityPhase flowActivityPhase;
    private PropertyUsage propertyUsage;
    private PropertyScope propertyScope;

    /**
     * A set of {@link FlowPropertyExpectation}s that this property definition needs. Note that a {@link FlowPropertyExpectation} can be optional.
     */
    private Set<FlowPropertyExpectation> propertiesDependentOn;

    protected FlowPropertyDefinitionImpl(FlowPropertyExpectation clone) {
        // TODO : validate there is a name
        this.name = clone.getName();
        if ( isBlank(this.name) ) {
            throw new FlowConfigurationException("Property name cannot be null or blank");
        }
        // TODO : validate there are data types (but need to allow for the String.class being the data type)
        this.dataClassDefinition = (DataClassDefinition) clone.getDataClassDefinition().clone();
        if (isNotEmpty(clone.getAlternates())) {
            this.alternates = new HashSet<String>(clone.getAlternates());
        }
        this.autoCreate = clone.getAutoCreate();
        this.flowPropertyValueProvider = clone.getFlowPropertyValueProvider();
        this.flowPropertyValuePersister = clone.getFlowPropertyValuePersister();
        if ( clone.getInitial() != null) {
            this.initial =clone.getInitial();
        }
        if (clone.getExternalPropertyAccessRestriction() != null) {
            this.externalPropertyAccessRestriction =clone.getExternalPropertyAccessRestriction();
        }
        this.saveBack = clone.getSaveBack();
        this.flowActivityPhase = clone.getPropertyRequired();
        this.propertyUsage = clone.getPropertyUsage();
        this.propertyScope = clone.getPropertyScope();
        this.propertiesDependentOn = clone.getPropertiesDependentOn();
    }

    /**
     *
     * @param flowPropertyProvider
     * @return defaultObject Should not save default object in
     *         {@link FlowPropertyDefinitionImpl} if it is mutable.
     */
    @Override
    public Object getDefaultObject(FlowPropertyProvider flowPropertyProvider) {
        Object value = null;
        FlowPropertyValueProvider<? extends FlowPropertyProvider> propertyValueProvider = this.flowPropertyValueProvider;
        if ( propertyValueProvider != null) {
            // 6 may 2012 - PATM - I forgot exact reason for this check. I believe it was for case where 2 different FlowActivities defined
            // property with same name. This was important for proper interpretation of the serialized property.
            // this code predated to a large extend the generic global use of FlowPropertyValueProviders. (it may no longer be useful).
            // problem is that if a flow activity defined a property and then the property was promoted to be a flow wide property, (it appears)
            // that the only the original flowactivity could get the default.
            Class<? extends FlowPropertyProvider> expected = propertyValueProvider.getFlowPropertyProviderClass();
            if ( !(expected == null || expected.isAssignableFrom(flowPropertyProvider.getClass()))) {
                throw new FlowExecutionException(
                    this,": expected a ", expected, " but got a ", flowPropertyProvider.getClass());
            }
            try {
            	// HACK : casting. fix definition later.
                value = ((FlowPropertyValueProvider<FlowPropertyProvider>)propertyValueProvider).get(flowPropertyProvider, this);
            } catch(FlowException e) {
                throw e;
            } catch(Exception e) {
                throw new FlowExecutionException(this.getName()+": PropertyValueProvider threw an exception. propertyValueProvider="+propertyValueProvider, e);
            }
        } else {
            // TODO -- may still want to call this if flowPropertyValueProvider returns null.
            // for example the property type is a primitive.
            value = this.getDataClassDefinition().getFlowTranslator().getDefaultObject(flowPropertyProvider);
        }
        // TODO -- do we want to set the default object? or recalculate it each time?
        // might be important if the default object is to get modified or if a FPD is shared.
        return value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getMapKey() {
        return getName();
    }

    @Override
    public FlowTranslator<?> getTranslator() {
        return this.getDataClassDefinition().getFlowTranslator();
    }

    @Override
    public boolean isFlowTranslatorSet() {
    	return getDataClassDefinition().isFlowTranslatorSet();
    }

    @Override
    public String getInitial() {
        return this.initial;
    }

    @Override
    public Boolean getAutoCreate() {
        return this.autoCreate;
    }

    @Override
    public boolean isAutoCreate() {
        if (isDefaultAvailable()) {
            return true;
        }
        return isDefaultByClassAvailable();
    }

    /**
     * @return
     */
    private boolean isDefaultByClassAvailable() {
        if ( this.autoCreate != null) {
            return getBoolean(this.autoCreate);
        }
        if (!getDataClassDefinition().isCollection() ) {
            Class<?> dataClass = getDataClassDefinition().getDataClass();
            if (dataClass.isPrimitive() ) {
                return true;
            } else if (dataClass.isInterface() || dataClass.isEnum()) {
                return false;
            }
        }
        return false;
    }
    @Override
    public boolean isDefaultAvailable() {
        return this.flowPropertyValueProvider != null;
    }

    @Override
    public String toString() {
        return getName() +"(scope="+getPropertyScope()+", usage="+getPropertyUsage()+") :"+this.dataClassDefinition;
    }

    @Override
    public <T> String serialize(T object) {
        if ( this.getDataClassDefinition().getFlowTranslator() == null) {
            return null;
        } else {
            try {
                Object serialized = this.getDataClassDefinition().serialize(this, object);
                return ObjectUtils.toString(serialized, null);
            } catch (FlowPropertySerializationNotPossibleException e) {
                return null;
            }
        }
    }
    @Override
    public <T, W> W serialize(W outputWriter, T value) {
        try {
            return this.dataClassDefinition.serialize(this, outputWriter, value);
        } catch (FlowPropertySerializationNotPossibleException e) {
            return outputWriter;
        }
    }
    @Override
    @SuppressWarnings("unchecked")
    public <V> V deserialize(FlowPropertyProvider flowPropertyProvider, Object serializedObject) throws FlowException {
        return (V) this.getDataClassDefinition().deserialize(flowPropertyProvider, this, serializedObject);
    }

    /**
     * @return the propertyRequired
     */
    @Override
    public FlowActivityPhase getPropertyRequired() {
        return this.flowActivityPhase == null?FlowActivityPhase.optional:this.flowActivityPhase;
    }

    /**
     * @return the propertyUsage (default {@link PropertyUsage#consume}
     */
    @Override
    public PropertyUsage getPropertyUsage() {
        if ( isPropertyUsageSet() ) {
            return this.propertyUsage;
        } else {
            return this.getPropertyScope().getDefaultPropertyUsage();
        }
    }

    /**
     * @return true if the propertyUsage is explicitly set.
     */
    @Override
    public boolean isPropertyUsageSet() {
        return this.propertyUsage != null;
    }

    /**
     * @return the propertyScope
     */
    @Override
    public PropertyScope getPropertyScope() {
        return isPropertyScopeSet()?this.propertyScope:PropertyScope.flowLocal;
    }
    @Override
    public boolean isPropertyScopeSet() {
        return this.propertyScope != null;
    }

    // HACK -- to fix later...
    @Override
    public FlowPropertyDefinition initialize() {
        checkInitial(this.getInitial());
        return this;
    }

    /**
     * make sure the initial value can be deserialized to the expected type.
     */
    private void checkInitial(String value) {
        if ( !this.getDataClassDefinition().isDeserializable(this, value)) {
            throw new FlowException(this + " while checking initial value="+ value);
        }
    }

    /**
     * @return the dataClassDefinition
     */
    @Override
    public DataClassDefinition getDataClassDefinition() {
        return this.dataClassDefinition;
    }

    @Override
    public Class<? extends Object> getDataClass() {
        return getDataClassDefinition().getDataClass();
    }

    @Override
    public Set<String> getAlternates() {
        return this.alternates;
    }
    @Override
    public Set<String> getAllNames() {
        Set<String> allNames = new LinkedHashSet<String>();
        allNames.add(this.getName());
        if ( this.alternates != null) {
            allNames.addAll(getAlternates());
        }
        return allNames;
    }

    @Override
    public ExternalPropertyAccessRestriction getExternalPropertyAccessRestriction() {
        if( this.externalPropertyAccessRestriction != null) {
            return this.externalPropertyAccessRestriction;
        } else {
            return getPropertyUsage() == PropertyUsage.internalState? ExternalPropertyAccessRestriction.noAccess :
                ExternalPropertyAccessRestriction.noRestrictions;
        }
    }

    @Override
    public boolean isExportable() {
        return this.getExternalPropertyAccessRestriction().isExternalReadAccessAllowed();
    }

    public boolean isSensitive() {
        return !getExternalPropertyAccessRestriction().isExternalReadAccessAllowed();
    }

    @Override
    public boolean isAssignableFrom(Class<?> clazz) {
        return this.getDataClassDefinition().isAssignableFrom(clazz);
    }

    @Override
    public boolean isDataClassMergeable(FlowPropertyDefinition flowPropertyDefinition) {
        return getDataClassDefinition().isMergable(((FlowPropertyDefinitionImplementor)flowPropertyDefinition).getDataClassDefinition());
    }

    @Override
    public boolean isMergeable(FlowPropertyDefinition property) {
        if (!(property instanceof FlowPropertyDefinitionImplementor)) {
            return false;
        }
        FlowPropertyDefinitionImplementor source = (FlowPropertyDefinitionImplementor)property;
        boolean result = isDataClassMergeable(source);
        if ( result ) {
            result &=this.flowPropertyValueProvider == null || source.getFlowPropertyValueProvider() == null || this.flowPropertyValueProvider.equals(source.getFlowPropertyValueProvider());
            if ( result ) {
                result &= !isPropertyScopeSet() || !property.isPropertyScopeSet() || getPropertyScope() == property.getPropertyScope();
                result &= !isPropertyUsageSet() || !property.isPropertyUsageSet()
                    || getPropertyUsage().isChangeableTo(property.getPropertyUsage()) || property.getPropertyUsage().isChangeableTo(getPropertyUsage());
                // TODO: ExternalPropertyAccessRestriction
                if ( !result) {
//                    System.out.println("scope clash");
                }
            } else {
//                System.out.println("provider clash");
            }
        } else {
//            System.out.println("dataclass clash");
        }
        return result;
    }

    /**
     * Copy from property any fields not already set.
     *
     * TODO: Need to check {@link PropertyScope}!! How to handle activityLocal/flowLocals???
     * @param property
     * @return true if there is no conflict in the dataClass, true if
     *         this.dataClass cannot be assigned by previous.dataClass
     *         instances.
     */
    @Override
    public boolean merge(FlowPropertyDefinition property) {
        if ( property == null || this == property) {
            return true;
        } else if ( !isDataClassMergeable(property)) {
            return false;
        }
        FlowPropertyDefinitionImpl source = (FlowPropertyDefinitionImpl)property;
        boolean noMergeConflict = isMergeable(source);
        if (this.autoCreate == null && source.autoCreate != null) {
            this.autoCreate = source.autoCreate;
        }
        this.getDataClassDefinition().merge(source.dataClassDefinition);

        if ( isNotEmpty(source.alternates)) {
            if (this.alternates == null ) {
                this.alternates = new HashSet<String>(source.alternates);
            } else {
                this.alternates.addAll(source.alternates);
            }
        }

        if ( this.flowPropertyValueProvider == null && source.flowPropertyValueProvider != null ) {
            this.flowPropertyValueProvider =source.flowPropertyValueProvider;
        }
        if ( this.flowPropertyValuePersister == null && source.flowPropertyValuePersister != null ) {
            this.flowPropertyValuePersister =source.flowPropertyValuePersister;
        }
        // TODO: merging should handling chained notification.
        if ( this.flowPropertyValueChangeListeners == null && source.flowPropertyValueChangeListeners != null ) {
            this.flowPropertyValueChangeListeners = source.flowPropertyValueChangeListeners;
        }
        if (this.initial == null && source.initial != null) {
            this.initial = source.initial;
            checkInitial(this.initial);
        }
        if (this.saveBack == null && source.saveBack != null) {
            this.saveBack =source.saveBack;
        }
        if (this.externalPropertyAccessRestriction == null && source.externalPropertyAccessRestriction != null) {
            this.externalPropertyAccessRestriction =source.externalPropertyAccessRestriction;
        }
        if ( !isPropertyScopeSet() && source.isPropertyScopeSet()) {
            this.propertyScope =source.propertyScope;
        }
        if ( source.isPropertyUsageSet()) {
            this.propertyUsage = PropertyUsage.survivingPropertyUsage(this.propertyUsage, source.propertyUsage);
        }

        // TODO : determine how to handle propertyRequired / PropertyUsage/PropertyScope/ExternalPropertyAccessRestriction which vary between different FAs in the same Flow.
        return noMergeConflict;
    }

    @Override
    public Boolean getSaveBack() {
        return this.saveBack;
    }

    @Override
    public boolean isSaveBack() {
        return getBoolean(this.saveBack);
    }

    @Override
    public boolean isCopyBackOnFlowSuccess() {
        // TODO: This needs to be a more complex test involving ExternalPropertyAccessRestriction as well.
        return getPropertyUsage().isOutputedProperty();
    }

    private boolean getBoolean(Boolean b) {
        return b != null && b;
    }

    @Override
    public boolean isCacheOnly() {
        return getPropertyScope().isCacheOnly();
    }

    @Override
    public boolean isReadOnly() {
        return getPropertyUsage().getAltersProperty() == Boolean.FALSE;
    }

    /**
     * @param <FA>
     * @param flowPropertyValueProvider the flowPropertyValueProvider to set
     */
    @Override
    @SuppressWarnings("unchecked")
    public <FA extends FlowPropertyProvider>void setFlowPropertyValueProvider(FlowPropertyValueProvider<FA> flowPropertyValueProvider) {
        // TODO: lock down for templates
        this.flowPropertyValueProvider = (FlowPropertyValueProvider<FlowPropertyProvider>) flowPropertyValueProvider;
    }

    /**
     * @param <FA>
     * @return the flowPropertyValueProvider
     */
    @Override
    @SuppressWarnings("unchecked")
    public <FA extends FlowPropertyProvider> FlowPropertyValueProvider<FA> getFlowPropertyValueProvider() {
        return (FlowPropertyValueProvider<FA>)this.flowPropertyValueProvider;
    }

    /**
     * @return the flowPropertyValuePersister
     */
    @Override
    public FlowPropertyValuePersister<FlowPropertyProvider> getFlowPropertyValuePersister() {
        return this.flowPropertyValuePersister;
    }

    @Override
    public List<Object> getObjectsNeedingToBeWired() {
        List<Object> objectsNeedingToBeWired = new ArrayList<Object>();
        addAllNotNull(objectsNeedingToBeWired, getFlowPropertyValueChangeListeners());
        addAllNotNull(objectsNeedingToBeWired, getTranslator(), getElementFlowTranslator(), getKeyFlowTranslator(), getFlowPropertyValuePersister(), getFlowPropertyValueProvider());
        return objectsNeedingToBeWired;
    }

    private FlowTranslator getKeyFlowTranslator() {
        DataClassDefinition keyDataClassDefinition = getDataClassDefinition().getKeyDataClassDefinition();
        return keyDataClassDefinition != null ? keyDataClassDefinition.getFlowTranslator() : null;
    }

    private FlowTranslator getElementFlowTranslator() {
        DataClassDefinition elementDataClassDefinition = getDataClassDefinition().getElementDataClassDefinition();
        return elementDataClassDefinition != null ? elementDataClassDefinition.getFlowTranslator() : null;
    }

    /**
     * @return the flowPropertyValueChangeListener
     */
    @Override
    public List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners() {
        return this.flowPropertyValueChangeListeners;
    }

    /**
     * @return the propertiesDependentOn
     */
    @Override
    public Set<FlowPropertyExpectation> getPropertiesDependentOn() {
        return this.propertiesDependentOn;
    }

    @Override
    public boolean isDynamic() {
        if (this.flowPropertyValueChangeListeners != null) {
            for (FlowPropertyValueChangeListener flowPropertyValueChangeListener: this.flowPropertyValueChangeListeners) {
                if ( isDynamic(flowPropertyValueChangeListener)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDynamic(Object object) {
        if ( object instanceof PossibleDynamic) {
            return ((PossibleDynamic)object).isDynamic();
        } else {
            return false;
        }
    }
    @Override
    public boolean equals(Object o) {
        if ( o == null || ! (o instanceof FlowPropertyDefinitionImpl)) {
            return false;
        } else if (o == this) {
            return true;
        }
        FlowPropertyDefinitionImpl flowPropertyDefinition = (FlowPropertyDefinitionImpl) o;
        EqualsBuilder equalsBuilder = new EqualsBuilder()
            .append(this.alternates, flowPropertyDefinition.alternates)
            .append(this.autoCreate, flowPropertyDefinition.autoCreate)
            .append(this.dataClassDefinition, flowPropertyDefinition.dataClassDefinition)
            .append(this.getExternalPropertyAccessRestriction(), flowPropertyDefinition.getExternalPropertyAccessRestriction())
            .append(this.flowPropertyValueProvider, flowPropertyDefinition.flowPropertyValueProvider)
            .append(this.flowPropertyValuePersister, flowPropertyDefinition.getFlowPropertyValuePersister())
            .append(this.flowPropertyValueChangeListeners, flowPropertyDefinition.getFlowPropertyValueChangeListeners())
            .append(this.initial, flowPropertyDefinition.initial)
            .append(this.name, flowPropertyDefinition.name)
                // use getter so that defaults can be calculated.
            .append(this.getPropertyRequired(), flowPropertyDefinition.getPropertyRequired())
            .append(this.getPropertyUsage(), flowPropertyDefinition.getPropertyUsage())
            .append(this.getPropertyScope(), flowPropertyDefinition.getPropertyScope())
            .append(this.propertiesDependentOn, flowPropertyDefinition.getPropertiesDependentOn())
            .append(this.saveBack, flowPropertyDefinition.saveBack);
        return equalsBuilder.isEquals();
    }

    /**
     * @see org.amplafi.flow.FlowPropertyDefinition#isNamed(java.lang.String)
     */
    @Override
    public boolean isNamed(String possiblePropertyName) {
        if ( isBlank(possiblePropertyName)) {
            return false;
        } else {
            return getName().equals(possiblePropertyName) ||
                    (this.getAlternates() != null && this.getAlternates().contains(possiblePropertyName));
        }
    }


    @Override
    public boolean isNamed(Class<?> byClassName) {
        return this.isNamed(FlowPropertyDefinitionBuilder.toPropertyName(byClassName));
    }

    @Override
    public boolean isApplicable(FlowPropertyExpectation flowPropertyExpectation) {
        return this.isNamed(flowPropertyExpectation.getName());
    }
    @Override
    public String getNamespaceKey(FlowState flowState, FlowPropertyProvider flowPropertyProvider) {
        if( flowPropertyProvider == null) {
            return this.getNamespaceKey(flowState);
        } else if ( flowPropertyProvider instanceof FlowActivity) {
            return this.getNamespaceKey((FlowActivity)flowPropertyProvider);
        } else if ( flowPropertyProvider instanceof FlowState) {
            FlowImplementor flow = ((FlowState)flowPropertyProvider).getFlow();
            return this.getNamespaceKey(flow);
        } else {
            return this.getNamespaceKey((FlowImplementor)flowPropertyProvider);
        }
    }
    private String getNamespaceKey(FlowActivity flowActivity) {
        switch ( this.getPropertyScope()) {
        case activityLocal:
            // TODO should really be ? but what about morphing??
//            list.add(flowActivity.getFullActivityName());
            return flowActivity.getFullActivityInstanceNamespace();
        case flowLocal:
        case requestFlowLocal:
            return getNamespaceKey((FlowImplementor)flowActivity.getFlow());
        case global:
            return null;
        default:
            throw new IllegalStateException(flowActivity.getFlowPropertyProviderFullName()+":"+this+":"+this.getPropertyScope()+": don't know namespace.");
        }
    }

    private String getNamespaceKey(FlowImplementor flow) {
        switch ( this.getPropertyScope()) {
        case flowLocal:
        case requestFlowLocal:
            if ( flow.getFlowState() != null) {
                return getNamespaceKey(flow.getFlowState());
            } else {
                return flow.getFlowPropertyProviderName();
            }
        case global:
            return null;
        default:
            throw new IllegalStateException(flow.getFlowPropertyProviderFullName()+":"+this+":"+this.getPropertyScope()+": don't know namespace.");
        }
    }

    private String getNamespaceKey(FlowState flowState) {
        switch (this.getPropertyScope()) {
        case activityLocal:
            throw new IllegalStateException(this + ":" + this.getPropertyScope() + " cannot be used when supplying only a FlowState");
        case flowLocal:
        case requestFlowLocal:
            return flowState.getLookupKey();
        case global:
            return null;
        default:
            throw new IllegalStateException(this.getPropertyScope() + ": don't know namespace.");
        }
    }

    @Override
    public List<String> getNamespaceKeySearchList(FlowState flowState, FlowPropertyProvider flowPropertyProvider, boolean forceAll) {
        if( flowPropertyProvider == null) {
            return this.getNamespaceKeySearchList(flowState, forceAll);
        } else if ( flowPropertyProvider instanceof FlowActivity) {
            return this.getNamespaceKeySearchList((FlowActivity)flowPropertyProvider, forceAll);
        } else {
            FlowImplementor flow;
            if ( flowPropertyProvider instanceof FlowState) {
                flow = ((FlowState)flowPropertyProvider).getFlow();
            } else if ( flowPropertyProvider instanceof FlowImplementor) {
                flow = (FlowImplementor)flowPropertyProvider;
            } else {
                throw new FlowConfigurationException(flowPropertyProvider, " is not a FlowState or FlowImplementor");
            }
            return this.getNamespaceKeySearchList(flow, forceAll);
        }
    }

    private List<String> getNamespaceKeySearchList(FlowImplementor flow, boolean forceAll) {
        //TODO have hierarchy of namespaces
        List<String> list = new ArrayList<String>();
        list.add(getNamespaceKey(flow));
        if ( getPropertyUsage().isExternallySettable() || forceAll) {
            switch(this.getPropertyScope()) {
            case flowLocal:
            case requestFlowLocal:
                list.add(flow.getFlowPropertyProviderName());
                list.add(null);
            case global:
                break;
            default:
                throw new IllegalStateException(this.getPropertyScope()+": don't know namespace.");
            }
        }
        return list;
    }
    /**
     * see notes on {@link #getNamespaceKeySearchList(FlowActivity, boolean)}
     * @param flowState
     * @param forceAll TODO
     * @param forExport TODO
     * @return list of namespace to search in order
     */
    private List<String> getNamespaceKeySearchList(FlowState flowState, boolean forceAll) {
        //TODO have hierarchy of namespaces
        List<String> list = new ArrayList<String>();
        list.add(getNamespaceKey(flowState));
        switch (this.getPropertyScope()) {
        case activityLocal:
            throw new IllegalStateException(this + ":" + this.getPropertyScope() + " cannot be used when supplying only a FlowState");
        case flowLocal:
        case requestFlowLocal:
            list.add(flowState.getFlow().getFlowPropertyProviderName());
            if (getPropertyUsage().isExternallySettable() || forceAll) {
                list.add(null);
            }
        case global:
            break;
        default:
            throw new IllegalStateException(flowState.getLookupKey()+":"+this+":"+this.getPropertyScope()+": don't know namespace.");
        }
        return list;
    }
    /**
     * Ideally we have a hierarchy of namespaces that can be checked for values. This works o.k. for searching for a value.
     * But problem is once the value is found, should the old value be replaced in whatever name space? should the found value be moved to the more exact namespace?
     *
     * Example, if a activityLocal can look at namespaces={flowstate.lookupKey+activity.activityName, flow.typename+activity.activityname, etc.} should any found value
     * be moved to flowstate.lookupKey+activity.activityName?
     *
     * What about morphing which would change the flowtype and thus the flowtype+activity.activityName namespace value. Morphing would result in loosing activity local values.
     *
     * right now flow morph does loose flowLocal values.
     *
     * Maybe when a flow is created the name space gets set for all flowLocal and activityLocal based on the flowstate lookup key?
     *
     * More thought definitely needed.
     *
     * Seems like the namespace list should vary depending on if we are initializing a flow from external parameters. An external setting would not like
     * to have to worry about using a flowState's lookupKey to set the namespace. Would prefer to just use the key with no namespace, or maybe a more generic
     * namespace like FlowTypeName or ActivityName.
     * @param flowActivity
     * @param forceAll TODO
     * @return list of namespaces to search in order
     */
    private List<String> getNamespaceKeySearchList(FlowActivity flowActivity, boolean forceAll) {
        //TODO have hierarchy of namespaces
        List<String> list = new ArrayList<String>();
        list.add(getNamespaceKey(flowActivity));
        if ( getPropertyUsage().isExternallySettable() || forceAll) {
            switch(this.getPropertyScope()) {
            case activityLocal:
                // TODO should really be ? but what about morphing??
                list.add(flowActivity.getFlowPropertyProviderFullName());
                list.add(flowActivity.getFlowPropertyProviderName());
            case flowLocal:
            case requestFlowLocal:
                list.add(flowActivity.getFlow().getFlowPropertyProviderName());
                list.add(null);
            case global:
                break;
            default:
                throw new IllegalStateException(this.getPropertyScope()+": don't know namespace.");
            }
        }
        return list;
    }

    @Override
    public FlowPropertyDefinitionImpl merge(FlowPropertyExpectation flowPropertyExpectation) {
	    throw new UnsupportedOperationException();
    }
    public FlowPropertyDefinitionImpl merge(Iterable<FlowPropertyExpectation> flowPropertyExpectations) {
        FlowPropertyDefinitionImpl result = this;
        for(FlowPropertyExpectation flowPropertyExpectation : NotNullIterator.<FlowPropertyExpectation>newNotNullIterator(flowPropertyExpectations)) {
            result = result.merge(flowPropertyExpectation);
        }
        return result;
    }
}
