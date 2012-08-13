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
import java.util.Collection;
import java.util.Collections;
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
import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowStepDirection;
import org.amplafi.flow.translator.FlowTranslator;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JsonSelfRenderer;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationIllegalStateException;
import com.sworddance.util.ApplicationNullPointerException;
import com.sworddance.util.CUtilities;
import com.sworddance.util.NotNullIterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import static org.apache.commons.lang.StringUtils.*;


/**
 * Defines a property that will be assigned as part of a {@link Flow} or
 * {@link FlowActivity}. This allows the value to be available to the component
 * or page referenced by a {@link FlowActivity}.
 *
 * TODO:
 * * FPD have a flag that makes them read-only
 * * if the FPD is "changed" then then method that changes the FPD should returned a cloned FPD with the modification. (see the FPD.init* methods() )
 * * necessary to allow instances of Flows to modify the values on a per flow-instance basis.
 *
 * @author Patrick Moore
 */
public class FlowPropertyDefinitionImpl extends AbstractFlowPropertyDefinitionProvider implements FlowPropertyDefinitionImplementor, Cloneable/*, FlowPropertyDefinitionProvider*/ {

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

    /**
     * Used when there is no explicit flowPropertyValueProvider. Primary usecase is FlowProperties that have a default
     * way of determining their value. But wish to allow that default method to be changed. (for example, fsFinishText )
     */
    private transient FlowPropertyValueProvider<? extends FlowPropertyProvider> factoryFlowPropertyValueProvider;

    // TODO: if a list of providers is defined then it becomes impossible to determine the minimum set of dependencies.
    // dependencies required by alternative FPVP may never be needed because the FPVP is never called.
    private transient FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider;
    private transient FlowPropertyValuePersister<FlowPropertyProvider> flowPropertyValuePersister;
    private transient List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners = new CopyOnWriteArrayList<FlowPropertyValueChangeListener>();
    /**
     * Used if the UI component's parameter name is different from the FlowPropertyDefinition's name.
     * Useful when using a FlowActivity with components that cannot be changed or have not been changed.
     * For example, a standard tapestry or tacos component.
     * Or a component that is used in multiple places and changing the UI component itself could cause a ripple of
     * cascading problems and possible regressions.
     */
    private String uiComponentParameterName;

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

    /**
     * A string meaningful to the UI framework
     */
    private String validators;
    private FlowActivityPhase flowActivityPhase;
    private PropertyUsage propertyUsage;
    private PropertyScope propertyScope;
    /**
     * TODO: Need to make more extensive and correct use.
     * once set no further changes to this {@link FlowPropertyDefinitionImpl} are permitted.
     * calling init* methods will return a new {@link FlowPropertyDefinitionImpl} that can be modified.
     * calling set* will result in an exception.
     */
    @Deprecated // need to use FlowPropertyDefinitionBuilder and make FPDI immutable.
    private boolean templateFlowPropertyDefinition;

    /**
     * A set of {@link FlowPropertyExpectation}s that this property definition needs. Note that a {@link FlowPropertyExpectation} can be optional.
     */
    private Set<FlowPropertyExpectation> propertiesDependentOn;

    /**
     * Creates an unnamed String property.
     */
    public FlowPropertyDefinitionImpl() {
        super((FlowPropertyDefinitionImplementor)null);
        this.dataClassDefinition = new DataClassDefinitionImpl();
    }

    public FlowPropertyDefinitionImpl(FlowPropertyDefinitionImpl clone) {
        super((FlowPropertyDefinitionImplementor)null);
        this.setName(clone.name);
        this.dataClassDefinition = (DataClassDefinition) clone.dataClassDefinition.clone();
        if (isNotEmpty(clone.alternates)) {
            this.alternates = new HashSet<String>();
            this.alternates.addAll(clone.alternates);
        }
        this.autoCreate = clone.autoCreate;
        this.factoryFlowPropertyValueProvider = clone.factoryFlowPropertyValueProvider;
        this.flowPropertyValueProvider = clone.flowPropertyValueProvider;
        this.flowPropertyValuePersister = clone.flowPropertyValuePersister;
        this.setInitial(clone.initial);
        this.setUiComponentParameterName(clone.uiComponentParameterName);
        if (clone.externalPropertyAccessRestriction != null) {
            this.setExternalPropertyAccessRestriction(clone.externalPropertyAccessRestriction);
        }
        this.validators = clone.validators;
        this.saveBack = clone.saveBack;
        this.flowActivityPhase = clone.flowActivityPhase;
        this.propertyUsage = clone.propertyUsage;
        this.propertyScope = clone.propertyScope;
    }

    /**
     * Creates an optional string property.
     *
     * @param name The name of the property.
     */
    public FlowPropertyDefinitionImpl(String name) {
        super((FlowPropertyDefinitionImplementor)null);
        this.dataClassDefinition = new DataClassDefinitionImpl();
        this.setName(name);
    }

    /**
     * Creates an optional property of the given type.
     *
     * @param name
     * @param dataClass
     * @param collectionClasses
     */
    public FlowPropertyDefinitionImpl(String name, Class<?> dataClass, Class<?>...collectionClasses) {
        this(name, dataClass, FlowActivityPhase.optional, collectionClasses);
    }

    /**
     * Creates a string property of the given requirements.
     *
     * @param name
     * @param requiredFlowActivityPhase
     */
    public FlowPropertyDefinitionImpl(String name, FlowActivityPhase requiredFlowActivityPhase) {
        this(name, null, requiredFlowActivityPhase);
    }

    /**
     * Creates a property of the given type and requirements.
     *
     * @param name
     * @param dataClass the underlying class that all the collection classes are wrapping.
     * @param requiredFlowActivityPhase when is this property required to be set
     * @param collectionClasses
     */
    public FlowPropertyDefinitionImpl(String name, Class<? extends Object> dataClass, FlowActivityPhase requiredFlowActivityPhase, Class<?>...collectionClasses) {
        this(name, requiredFlowActivityPhase, new DataClassDefinitionImpl(dataClass, collectionClasses));
    }
    public FlowPropertyDefinitionImpl(String name, DataClassDefinition dataClassDefinition) {
        this(name, FlowActivityPhase.optional, dataClassDefinition);
    }
    public FlowPropertyDefinitionImpl(String name, FlowActivityPhase requiredFlowActivityPhase, DataClassDefinition dataClassDefinition) {
        super((FlowPropertyDefinitionImplementor)null);
        this.setName(name);
        this.flowActivityPhase = requiredFlowActivityPhase;
        this.dataClassDefinition = dataClassDefinition;
    }

    @Deprecated //  TODO: move initDefaultObject() to FlowPropertyDefinitionFactory
    @SuppressWarnings("unchecked")
    public void setDefaultObject(Object defaultObject) {
        if ( !(defaultObject instanceof FlowPropertyValueProvider<?>)) {
            FixedFlowPropertyValueProvider fixedFlowPropertyValueProvider = FixedFlowPropertyValueProvider.newFixedFlowPropertyValueProvider(defaultObject, this, true);
            this.flowPropertyValueProvider = fixedFlowPropertyValueProvider;
        } else {
            this.flowPropertyValueProvider = (FlowPropertyValueProvider<FlowPropertyProvider>)defaultObject;
        }
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
        ApplicationNullPointerException.notNull(flowPropertyProvider,this,"flowPropertyProvider cannot be null");
        FlowPropertyValueProvider<? extends FlowPropertyProvider> propertyValueProvider = getDefaultFlowPropertyValueProviderToUse();
        if ( propertyValueProvider != null) {
            // 6 may 2012 - PATM - I forgot exact reason for this check. I believe it was for case where 2 different FlowActivities defined
            // property with same name. This was important for proper interpretation of the serialized property.
            // this code predated to a large extend the generic global use of FlowPropertyValueProviders. (it may no longer be useful).
            // problem is that if a flow activity defined a property and then the property was promoted to be a flow wide property, (it appears)
            // that the only the original flowactivity could get the default.
            Class<? extends FlowPropertyProvider> expected = propertyValueProvider.getFlowPropertyProviderClass();
            ApplicationIllegalArgumentException.valid(expected == null || expected.isAssignableFrom(flowPropertyProvider.getClass()),
                this,": expected a ", expected, " but got a ", flowPropertyProvider.getClass());
            try {
            	// HACK : casting. fix definition later.
                value = ((FlowPropertyValueProvider<FlowPropertyProvider>)propertyValueProvider).get(flowPropertyProvider, this);
            } catch(Exception e) {
                throw new ApplicationIllegalStateException(this+": PropertyValueProvider threw an exception. propertyValueProvider="+propertyValueProvider, e);
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
    public boolean isDefaultObjectAvailable(FlowPropertyProvider flowPropertyProvider) {
        FlowPropertyValueProvider<? extends FlowPropertyProvider> propertyValueProvider = getDefaultFlowPropertyValueProviderToUse();
        if ( propertyValueProvider != null) {
            Class<? extends FlowPropertyProvider> expected = propertyValueProvider.getFlowPropertyProviderClass();
            if (  expected.isAssignableFrom(flowPropertyProvider.getClass())) {
                return true;
            } else {
                return false;
            }
        } else {
            // slight HACK as we should really check first and not actually get the default object.
            return false;//this.getDataClassDefinition().getFlowTranslator().getDefaultObject(flowPropertyProvider) != null;
        }
    }

    /**
     * @return
     */
    private FlowPropertyValueProvider<? extends FlowPropertyProvider> getDefaultFlowPropertyValueProviderToUse() {
      if (this.flowPropertyValueProvider != null ) {
            return this.flowPropertyValueProvider;
        } else {
            return this.factoryFlowPropertyValueProvider;
        }
    }

    @Override
    @Deprecated //  TODO: move initDefaultObject() to FlowPropertyDefinitionFactory
    public FlowPropertyDefinitionImpl initDefaultObject(Object defaultObject) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.flowPropertyValueProvider, defaultObject);
        flowPropertyDefinition.setDefaultObject(defaultObject);
        return flowPropertyDefinition;
    }

    @Override
    public FlowPropertyDefinitionImpl initFactoryFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.factoryFlowPropertyValueProvider, flowPropertyValueProvider);
    	flowPropertyDefinition.factoryFlowPropertyValueProvider = flowPropertyValueProvider;
    	return flowPropertyDefinition;
    }

    public void setName(String name) {
    	this.name = this.setCheckTemplateState(this.name, name);
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
    public String getValidators() {
        return this.validators;
    }

    public void setValidators(String validators) {
    	this.validators = this.setCheckTemplateState(this.validators, validators);
    }

    // TODO fix with template check
    public FlowPropertyDefinitionImpl addValidator(String validator) {
    	if ( !StringUtils.isBlank(validator)) {
	        if (StringUtils.isBlank(this.validators)) {
	            return initValidators(validator);
	        } else {
	            return initValidators(this.validators + "," + validator);
	        }
	    }
    	return this;
    }

    public FlowPropertyDefinitionImpl validateWith(String... fields) {
        return initValidators("flowField="+join(fields,"-"));
    }

    /**
     * Sets validators for this definition. <p/>
     * Note that existing validators will be removed - if you
     * don't want that behavior, consider using {@link #addValidator(String)}.
     * @param validators
     * @return this or flowPropertyDefinition
     */
    public FlowPropertyDefinitionImpl initValidators(String validators) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.validators, validators);
        flowPropertyDefinition.setValidators(validators);
        return flowPropertyDefinition;
    }

    @Override
    public FlowTranslator<?> getTranslator() {
        return this.getDataClassDefinition().getFlowTranslator();
    }

    @Override
    public boolean isFlowTranslatorSet() {
    	return getDataClassDefinition().isFlowTranslatorSet();
    }

    public void setTranslator(FlowTranslator<?> flowTranslator) {
    	this.getDataClassDefinition().setFlowTranslator(
    			this.setCheckTemplateState(getTranslator(), flowTranslator));
    }

    @Override
    public FlowPropertyDefinitionImpl initTranslator(FlowTranslator<?> flowTranslator) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.getTranslator(), flowTranslator);
    	flowPropertyDefinition.setTranslator(flowTranslator);
        return flowPropertyDefinition;
    }

    /**
     * This is used to handle case of parameter name change and 'short' names
     * for uris and the like.
     *
     * @param alternateNames
     * @return this or clone
     */
    public FlowPropertyDefinitionImpl addAlternateNames(String... alternateNames) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.alternates, alternateNames);
        Collections.addAll(flowPropertyDefinition.getAlternates(),alternateNames);
        return flowPropertyDefinition;
    }

    public void setInitial(String initial) {
        this.initial = this.setCheckTemplateState(this.initial, initial);
        checkInitial(this.initial);
    }

    @Override
    public String getInitial() {
        return this.initial;
    }

    public FlowPropertyDefinitionImpl initInitial(String initialValue) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.initial, initialValue);
        flowPropertyDefinition.setInitial(initialValue);
        return flowPropertyDefinition;
    }

    public void setUiComponentParameterName(String uiComponentParameterName) {
    	this.uiComponentParameterName = this.setCheckTemplateState(this.uiComponentParameterName, uiComponentParameterName);
    }

    @Override
    public String getUiComponentParameterName() {
        if (this.uiComponentParameterName == null) {
            return getName();
        }
        return this.uiComponentParameterName;
    }

    public FlowPropertyDefinitionImpl initParameterName(String uiComponentParameterName) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.uiComponentParameterName, uiComponentParameterName);
        flowPropertyDefinition.setUiComponentParameterName(uiComponentParameterName);
        return flowPropertyDefinition;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
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
        return this.getDefaultFlowPropertyValueProviderToUse() != null;
    }

    @Override
    public String toString() {
        return toComponentDef() +"(scope="+getPropertyScope()+", usage="+getPropertyUsage()+") :"+this.dataClassDefinition;
    }

    public String toComponentDef() {
        return getUiComponentParameterName();
    }

    public static String toString(String paramName, String flowPropName) {
        return " " + paramName + "=\"fprop:" + flowPropName + "\" ";
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
    public <T> IJsonWriter serialize(IJsonWriter jsonWriter, T value) {
        try {
            return this.dataClassDefinition.serialize(this, jsonWriter, value);
        } catch (FlowPropertySerializationNotPossibleException e) {
            return jsonWriter;
        }
    }
    @Override
    @SuppressWarnings("unchecked")
    public <V> V parse(FlowPropertyProvider flowPropertyProvider, String value) throws FlowException {
        return (V) this.getDataClassDefinition().deserialize(flowPropertyProvider, this, value);
    }
    /**
     * @param flowActivityPhase the propertyRequired to set
     * @return this
     */
    @Override
    public FlowPropertyDefinitionImpl initPropertyRequired(FlowActivityPhase flowActivityPhase) {
        if ( !FlowActivityPhase.isSameAs(this.flowActivityPhase, flowActivityPhase)){
        	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.flowActivityPhase, flowActivityPhase);
        	flowPropertyDefinition.setPropertyRequired(flowActivityPhase);
            return flowPropertyDefinition;
        } else {
            return this;
        }
    }
    /**
     * @param flowActivityPhase the propertyRequired to set
     */
    @Override
    public void setPropertyRequired(FlowActivityPhase flowActivityPhase) {
    	this.flowActivityPhase = this.setCheckTemplateState(this.flowActivityPhase, flowActivityPhase);
    }

    /**
     * @return the propertyRequired
     */
    @Override
    public FlowActivityPhase getPropertyRequired() {
        return this.flowActivityPhase == null?FlowActivityPhase.optional:this.flowActivityPhase;
    }

    /**
     * @param propertyUsage the propertyUsage to set
     */
    @Override
    public void setPropertyUsage(PropertyUsage propertyUsage) {
        if ( this.getPropertyScope().isAllowedPropertyUsage(propertyUsage)) {
            this.propertyUsage = setCheckTemplateState(this.propertyUsage, propertyUsage);
        } else {
            // throw exception?
        }
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
    @Override
    public FlowPropertyDefinitionImpl initPropertyUsage(PropertyUsage propertyUsage) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.propertyUsage, propertyUsage);
    	flowPropertyDefinition.setPropertyUsage(propertyUsage);
        return flowPropertyDefinition;
    }

    /**
     * @param propertyScope the propertyScope to set
     */
    @Override
    public void setPropertyScope(PropertyScope propertyScope) {
        this.propertyScope = setCheckTemplateState(this.propertyScope, propertyScope);
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
    @Override
    public FlowPropertyDefinitionImpl initPropertyScope(PropertyScope propertyScope) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.propertyScope, propertyScope);
    	flowPropertyDefinition.setPropertyScope(propertyScope);
        return flowPropertyDefinition;
    }
    @Override
    public FlowPropertyDefinitionImpl initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage) {
        return initAccess(propertyScope,propertyUsage,this.externalPropertyAccessRestriction.noRestrictions);
    }
    @Override
    public FlowPropertyDefinitionImpl initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage, ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        return initPropertyScope(propertyScope).initPropertyUsage(propertyUsage).initExternalPropertyAccessRestriction(externalPropertyAccessRestriction);
    }
    /**
     * affects isEntity()
     *
     * @param dataClass
     */
    public void setDataClass(Class<? extends Object> dataClass) {
        this.getDataClassDefinition().setDataClass(dataClass);
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
            throw new IllegalStateException(this + " while checking initial value="+ value);
        }
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
        return this.dataClassDefinition;
    }

    @Override
    public Class<? extends Object> getDataClass() {
        return getDataClassDefinition().getDataClass();
    }

    @Override
    public Set<String> getAlternates() {
        if (this.alternates == null) {
            this.alternates = new HashSet<String>();
        }
        return this.alternates;
    }
    @Override
    public Set<String> getAllNames() {
        Set<String> allNames = new LinkedHashSet<String>();
        allNames.add(this.getName());
        allNames.addAll(getAlternates());
        return allNames;
    }

    public void setExternalPropertyAccessRestriction(ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this.externalPropertyAccessRestriction = externalPropertyAccessRestriction;
    }

    public ExternalPropertyAccessRestriction getExternalPropertyAccessRestriction() {
        if( this.externalPropertyAccessRestriction != null) {
            return this.externalPropertyAccessRestriction;
        } else if ( this.name.startsWith("fs") || this.name.startsWith("fa")) {
            // HACK: PATM : To TIRIS: Start applying ExternalPropertyAccessRestriction.noAccess to these properties
            return ExternalPropertyAccessRestriction.noAccess;
        } else {
            return getPropertyUsage() == PropertyUsage.internalState? ExternalPropertyAccessRestriction.noAccess :
                ExternalPropertyAccessRestriction.noRestrictions;
        }
    }

    @Override
    public boolean isExportable() {
        return this.getExternalPropertyAccessRestriction() != ExternalPropertyAccessRestriction.noAccess;
    }

    public FlowPropertyDefinitionImpl initExternalPropertyAccessRestriction(ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.externalPropertyAccessRestriction, externalPropertyAccessRestriction);
    	flowPropertyDefinition.setExternalPropertyAccessRestriction(externalPropertyAccessRestriction);
        return flowPropertyDefinition;
    }
    @Override
    public FlowPropertyDefinitionImpl initSensitive() {
    	return initExternalPropertyAccessRestriction(ExternalPropertyAccessRestriction.noAccess);
    }

    public boolean isSensitive() {
        return !getExternalPropertyAccessRestriction().isExternalReadAccessAllowed();
    }
    /**
     * TODO: USE!
     * not a reversible process.
     */
    @Override
    public void setTemplateFlowPropertyDefinition() {
        this.templateFlowPropertyDefinition = true;
    }

    /**
     * @return the templateFlowPropertyDefinition
     */
    public boolean isTemplateFlowPropertyDefinition() {
        return this.templateFlowPropertyDefinition;
    }
    protected <T> T setCheckTemplateState(T oldObject, T newObject) {
        if ( this.templateFlowPropertyDefinition && !ObjectUtils.equals(oldObject, newObject)) {
            ApplicationIllegalStateException.checkState(false,
                this, "Cannot change state of a Template FlowPropertyDefinition. A property was going from ", oldObject," to ", newObject);
        }
        return newObject;
    }
    protected <T> FlowPropertyDefinitionImpl cloneIfTemplate(T oldObject, T newObject) {
        if ( this.templateFlowPropertyDefinition && !ObjectUtils.equals(oldObject, newObject) ) {
            return this.clone();
        }
        return this;
    }
    protected <E, T extends Collection<E>> FlowPropertyDefinitionImpl cloneIfTemplateAndNeedToAdd(T oldObject, E newObject) {
        if ( !this.templateFlowPropertyDefinition || newObject == null) {
            return this;
        } else if ( oldObject == null || !oldObject.contains(newObject)) {
            return this.clone();
        } else {
            // already contains
            return this;
        }
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
     * TODO: NOTE that merge explicitly is allowed to violate the {@link #templateFlowPropertyDefinition} lockdown rules
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
            this.setAutoCreate(source.autoCreate);
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
        if ( this.factoryFlowPropertyValueProvider == null && source.factoryFlowPropertyValueProvider != null ) {
            this.factoryFlowPropertyValueProvider =source.factoryFlowPropertyValueProvider;
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
        if (this.uiComponentParameterName == null && source.uiComponentParameterName != null) {
            this.uiComponentParameterName = source.uiComponentParameterName;
        }
        if (this.externalPropertyAccessRestriction == null && source.externalPropertyAccessRestriction != null) {
            this.externalPropertyAccessRestriction =source.externalPropertyAccessRestriction;
        }
        if (this.validators == null && source.validators != null) {
            this.validators =source.validators;
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

    @SuppressWarnings("unchecked")
    @Override
    public FlowPropertyDefinitionImpl clone() {
        return new FlowPropertyDefinitionImpl(this);
    }

    public void setSaveBack(Boolean saveBack) {
    	this.saveBack = this.setCheckTemplateState(this.saveBack, saveBack);
    }

    @Override
    public boolean isSaveBack() {
        if (this.saveBack != null) {
            return getBoolean(this.saveBack);
        } else {
            return getDataClassDefinition().isCollection() || JsonSelfRenderer.class.isAssignableFrom(getDataClassDefinition().getDataClass());
        }
    }

    @Override
    public boolean isCopyBackOnFlowSuccess() {
        // TODO: This needs to be a more complex test involving ExternalPropertyAccessRestriction as well.
        return getPropertyUsage().isOutputedProperty();
    }

    private boolean getBoolean(Boolean b) {
        return b != null && b;
    }

    /**
     * @see #saveBack
     * @param saveBack
     * @return this or clone
     */
    public FlowPropertyDefinitionImpl initSaveBack(Boolean saveBack) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.saveBack, saveBack);
    	flowPropertyDefinition.setSaveBack(saveBack);
        return flowPropertyDefinition;
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
     * Sets autoCreate to true - meaning that if the property does not exist in
     * the cache, a new instance is created. <p/> Uses
     * {@link #setAutoCreate(boolean)}
     *
     * @return this
     */
    public FlowPropertyDefinitionImpl initAutoCreate() {
        FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.autoCreate, Boolean.TRUE);
        flowPropertyDefinition.setAutoCreate(true);
        return flowPropertyDefinition;
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
     * @param flowPropertyValueProvider
     * @return this or a clone
     */
    @Override
    public FlowPropertyDefinitionImpl initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.flowPropertyValueProvider, flowPropertyValueProvider);
    	flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
        return flowPropertyDefinition;
    }

    /**
     * @return the flowPropertyValuePersister
     */
    @Override
    public FlowPropertyValuePersister<FlowPropertyProvider> getFlowPropertyValuePersister() {
        return this.flowPropertyValuePersister;
    }

    /**
     * @param <FA>
     * @param flowPropertyValuePersister the flowPropertyValuePersister to set
     */
    @Override
    @SuppressWarnings("unchecked")
    public <FA extends FlowPropertyProvider> void setFlowPropertyValuePersister(FlowPropertyValuePersister<FA> flowPropertyValuePersister) {
        this.flowPropertyValuePersister = (FlowPropertyValuePersister<FlowPropertyProvider>) flowPropertyValuePersister;
    }

    /**
     * @param flowPropertyValuePersister
     * @return this or a clone
     */
    @Override
    public <FPP extends FlowPropertyProvider> FlowPropertyDefinitionImpl initFlowPropertyValuePersister(FlowPropertyValuePersister<FPP> flowPropertyValuePersister) {
    	FlowPropertyDefinitionImpl flowPropertyDefinition = cloneIfTemplate(this.flowPropertyValuePersister, flowPropertyValuePersister);
    	flowPropertyDefinition.setFlowPropertyValuePersister(flowPropertyValuePersister);
        return flowPropertyDefinition;
    }


    @Override
    public List<Object> getObjectsNeedingToBeWired() {
        List<Object> objectsNeedingToBeWired = new ArrayList<Object>();
        CUtilities.addAllNotNull(objectsNeedingToBeWired, getFlowPropertyValueChangeListeners());
        CUtilities.addAllNotNull(objectsNeedingToBeWired, getTranslator(), getFlowPropertyValuePersister(), getFlowPropertyValueProvider(), this.factoryFlowPropertyValueProvider);
        return objectsNeedingToBeWired;
    }

    /**
     * @return the flowPropertyValueChangeListener
     */
    @Override
    public List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners() {
        return this.flowPropertyValueChangeListeners;
    }

    /**
     * @param flowPropertyValueChangeListeners the flowPropertyValueChangeListener to set
     */
    public void setFlowPropertyValueChangeListeners(List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners) {
        this.flowPropertyValueChangeListeners = this.setCheckTemplateState(this.flowPropertyValueChangeListeners, flowPropertyValueChangeListeners);
    }

    @Override
    public FlowPropertyDefinitionImpl initFlowPropertyValueChangeListener(FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = this.cloneIfTemplateAndNeedToAdd(this.flowPropertyValueChangeListeners, flowPropertyValueChangeListener);
        flowPropertyDefinition.flowPropertyValueChangeListeners.add(flowPropertyValueChangeListener);
        return flowPropertyDefinition;
    }
    @Override
    public FlowPropertyDefinitionImpl addFlowPropertyValueChangeListeners(Collection<FlowPropertyValueChangeListener> additionalFlowPropertyValueChangeListeners) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = this;
        for(FlowPropertyValueChangeListener flowPropertyValueChangeListener: NotNullIterator.<FlowPropertyValueChangeListener>newNotNullIterator(additionalFlowPropertyValueChangeListeners)) {
            flowPropertyDefinition = flowPropertyDefinition.initFlowPropertyValueChangeListener(flowPropertyValueChangeListener);
        }
        return flowPropertyDefinition;
    }
    /**
     * @param propertiesDependentOn the propertiesDependentOn to set
     */
    public void setPropertiesDependentOn(Set<FlowPropertyExpectation> propertiesDependentOn) {
        this.propertiesDependentOn = this.setCheckTemplateState(this.propertiesDependentOn, propertiesDependentOn);
    }

    /**
     * @return the propertiesDependentOn
     */
    @Override
    public Collection<FlowPropertyExpectation> getPropertiesDependentOn() {
        return this.propertiesDependentOn;
    }
    /**
     * Used in resource code
     * @param propertiesDependentOn
     * @return
     */
    @Override
    @SuppressWarnings("hiding")
    public FlowPropertyDefinitionImpl addPropertiesDependentOn(Collection<FlowPropertyExpectation> propertiesDependentOn) {
        if ( isNotEmpty(propertiesDependentOn)) {
            if ( this.propertiesDependentOn == null) {
                this.propertiesDependentOn = new HashSet<FlowPropertyExpectation>();
            }
            addAllNotNull(this.propertiesDependentOn, propertiesDependentOn);
        }
        return this;
    }
    /**
     * Used in resource code
     * @param propertiesDependentOn
     * @return
     */
    @Override
    @SuppressWarnings("hiding")
    public FlowPropertyDefinitionImpl addPropertiesDependentOn(FlowPropertyExpectation... propertiesDependentOn) {
        if ( isNotEmpty(propertiesDependentOn)) {
            if ( this.propertiesDependentOn == null) {
                this.propertiesDependentOn = new HashSet<FlowPropertyExpectation>();
            }
            addAllNotNull(this.propertiesDependentOn, propertiesDependentOn);
        }
        return this;
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
            .append(this.flowPropertyValueProvider, flowPropertyDefinition.flowPropertyValueProvider)
            .append(this.initial, flowPropertyDefinition.initial)
            .append(this.name, flowPropertyDefinition.name)
            .append(this.uiComponentParameterName, flowPropertyDefinition.uiComponentParameterName)
                // use getter so that defaults can be calculated.
            .append(this.getPropertyRequired(), flowPropertyDefinition.getPropertyRequired())
            .append(this.getPropertyUsage(), flowPropertyDefinition.getPropertyUsage())
            .append(this.getPropertyScope(), flowPropertyDefinition.getPropertyScope())
            .append(this.getExternalPropertyAccessRestriction(), flowPropertyDefinition.getExternalPropertyAccessRestriction())
            .append(this.saveBack, flowPropertyDefinition.saveBack)
            .append(this.validators, flowPropertyDefinition.validators);
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
                this.getAlternates().contains(possiblePropertyName);
        }
    }


    @Override
    public boolean isNamed(Class<?> byClassName) {
        return this.isNamed(FlowPropertyDefinitionBuilder.toPropertyName(byClassName));
    }

    @Override
    public boolean isApplicable(FlowPropertyDefinitionImplementor flowPropertyDefinition) {
        return this.isNamed(flowPropertyDefinition.getName());
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
                throw ApplicationIllegalArgumentException.fail(flowPropertyProvider, " is not a FlowState or FlowImplementor");
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

    /**
     *
     */
    @Override
    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        List<FlowPropertyDefinitionImplementor> clonedSelf = Arrays.asList((FlowPropertyDefinitionImplementor)this.clone());
        super.addPropertyDefinitions(flowPropertyProvider, clonedSelf, additionalConfigurationParameters);
    }

	@Override
	public IJsonWriter toJson(IJsonWriter jsonWriter) {
		jsonWriter.object();
		jsonWriter.keyValue("name", this.getName());
		jsonWriter.keyValue("type", this.getDataClass().getSimpleName());
		jsonWriter.keyValue("req", this.getPropertyRequired());
		jsonWriter.endObject();
		return jsonWriter;
	}

	@Override
	public <T> T fromJson(Object object) {
		return null;
	}


}
