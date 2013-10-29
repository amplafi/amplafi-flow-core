package org.amplafi.flow.flowproperty;

import static com.sworddance.util.CUtilities.addAllIfNotContains;
import static com.sworddance.util.CUtilities.getFirstNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowStepDirection;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.translator.FlowTranslator;
import org.amplafi.json.JsonSelfRenderer;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.sworddance.util.AbstractParameterizedCallableImpl;
import com.sworddance.util.NotNullIterator;
import com.sworddance.util.map.ConcurrentInitializedMap;

/**
 * Used to construct {@link FlowPropertyDefinition}s and {@link FlowPropertyExpectation}s by unioning a set of
 * standard {@link FlowPropertyExpectation}s and calling {@link FlowPropertyDefinitionBuilder}'s methods to describe the constraints on
 * the generated {@link FlowPropertyDefinition}.
 *
 * {@link FlowPropertyDefinitionBuilder} are not thread safe and should not be reused as each generated property is the accumulation of the
 * previously generated property. There is no general 'clear()' for this reason.
 *
 * This builder enables
 * {@link FlowPropertyDefinition}s and {@link FlowPropertyExpectation} to be immutable.
 *
 * Notes:
 * FlowPropertyDefinitionBuilder was introduced because of the problems of having a
 * FlowPropertyDefinition that can be endlessly modified, which then resulted in FPD deciding if
 * they were templates (immutable) or not. With a builder, all {@link FlowPropertyDefinition}s
 * become immutable.
 *
 * TODO: make {@link FlowPropertyDefinitionImpl} immutable.
 * Any changes to a FPD require a builder to construct a new FPD.
 *
 * TODO: ? add a way to make an attempt to read an unset property fail rather than return null? Useful for
 * {@link PropertyUsage#getAltersProperty()} == TRUE
 *
 * TODO: remove initDefaultObject() Handles some common use cases
 *
 * @author patmoore
 */
public class FlowPropertyDefinitionBuilder {
    /**
     * only set in constructor but not final because could be building a {@link FlowPropertyExpectation} which does not require a name.
     */
    private String name;
    private FlowActivityPhase propertyRequired;
    private PropertyScope propertyScope;
    private PropertyUsage propertyUsage;

    // TODO list for a chained series of possible providers
    private FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider;
    private FlowPropertyValuePersister flowPropertyValuePersister;
    private List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners;

    private ExternalPropertyAccessRestriction externalPropertyAccessRestriction;

    private DataClassDefinitionImpl dataClassDefinition;

    private Set<String> alternates;
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
    private Object defaultObject;

    /**
     * A set of {@link FlowPropertyExpectation}s that this property definition needs. Note that a {@link FlowPropertyExpectation} can be optional.
     */
    private Set<FlowPropertyExpectation> propertiesDependentOn;

    private String initial;

    private static final Map<Class<?>, String> propertyNameFromClassName = new ConcurrentInitializedMap<>(new AbstractParameterizedCallableImpl<String>() {

        @Override
        public String executeCall(Object... parameters) throws Exception {
            Class<?> clazz = getKeyFromParameters(parameters);
            String simpleName = clazz.getSimpleName();
            String propertyName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
            return propertyName;
        }
    });

    // ========================================================================================================================
    //  These are setup as separate FPE rather than conv. methods because these expectations are applied to property definitions
    // for the specific use of the property. ( i.e. the same property could be an input, output, initialized,
    // required, optional, etc depending on the specific flow using the property.
    // ========================================================================================================================
    /**
     * A property that is not allowed to be externally altered. (no external set is allowed) But the property is not
     * immutable because a FPVP could supply different values.
     *
     * Use case: User id
     *
     * Specifically:
     * {@link PropertyUsage#initialize}, ExternalPropertyAccessRestriction.readonly
     * Expectation is that {@link FlowPropertyValueProvider} will be supplied later.
     * {@link #readOnly()} - applies this expectation.
     */
    public static List<FlowPropertyExpectation> READ_ONLY_VALUE = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly));
    /**
     * Like {@link #READ_ONLY_VALUE}, except specifies {@link FlowActivityPhase#finish}
     * To return a api value,
     * 1) the property must be initialized when the call completes,
     * 2) the property must local to at least flow
     * 3) the property will not be altered.
     */
    public static List<FlowPropertyExpectation> API_RETURN_VALUE = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.finish, PropertyScope.flowLocal, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly));

    /**
     * Allows to optionally include properties in final flow state. The property only goes out if it was accessed before flow finish.
     */
    public static List<FlowPropertyExpectation> API_OPTIONAL_RETURN_VALUE = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.optional, PropertyScope.flowLocal, PropertyUsage.io, ExternalPropertyAccessRestriction.readonly));
    /**
     * The property is available for edit or if missing can be created.
     */
    public static List<FlowPropertyExpectation> CREATE_OR_EDIT = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.finish, PropertyScope.flowLocal, PropertyUsage.createsIfMissing, ExternalPropertyAccessRestriction.noRestrictions));
    public static List<FlowPropertyExpectation> IO = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.io, null));
    public static List<FlowPropertyExpectation> REQUIRED_INPUT_CONSUMING = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.activate, null, PropertyUsage.consume, null));

    /**
     * An optional api parameter.
     */
    public static List<FlowPropertyExpectation> OPTIONAL_INPUT = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.optional, PropertyScope.flowLocal, PropertyUsage.use, null));
    public static List<FlowPropertyExpectation> CONSUMING = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.consume, null));
    /**
     *  not externally settable by user but can be set by previous flow.
     */
    public static List<FlowPropertyExpectation> INTERNAL_ONLY = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.internalState, ExternalPropertyAccessRestriction.noAccess));
    public static List<FlowPropertyExpectation> SECURED = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, null, ExternalPropertyAccessRestriction.noAccess));

    /**
     * create a {@link FlowPropertyDefinition} for a property whose value is recomputed for every
     * request. Use case: very dynamic properties for example, a status message.
     */
    public static List<FlowPropertyExpectation> REQUEST_ONLY = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, PropertyScope.requestFlowLocal, PropertyUsage.suppliesIfMissing, ExternalPropertyAccessRestriction.noRestrictions));
    public static List<FlowPropertyExpectation> GENERATED_KNOWN_FA = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(null, null, PropertyUsage.consume, ExternalPropertyAccessRestriction.noAccess));
    /**
     * A security property: we want the caller to be able to supply a value but to not be able to read it.
     * Best Use case: setting a password or supplying a password for login - we don't want any access to be able to read a password.
     *
     */
    public static List<FlowPropertyExpectation> WRITE_ONLY = Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.activate,
        PropertyScope.flowLocal, PropertyUsage.consume, ExternalPropertyAccessRestriction.writeonly));
    /**
     * because name can only be set in ctor - this is used when construction {@link FlowPropertyExpectation}
     */
    public FlowPropertyDefinitionBuilder() {

    }
    public FlowPropertyDefinitionBuilder(String name) {
        this.name = name;
    }
    public FlowPropertyDefinitionBuilder(String name, DataClassDefinitionImpl dataClassDefinition) {
        this(name);
        this.dataClassDefinition = dataClassDefinition.clone();
    }

    public FlowPropertyDefinitionBuilder(FlowPropertyExpectation flowPropertyExpectation) {
        this.name = flowPropertyExpectation.getName();
        this.applyFlowPropertyExpectation(flowPropertyExpectation);
    }
    public FlowPropertyDefinitionBuilder(FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder) {
        FlowPropertyExpectation flowPropertyExpectation = flowPropertyDefinitionBuilder.toFlowPropertyExpectation();
        this.name = flowPropertyExpectation.getName();
        this.applyFlowPropertyExpectation(flowPropertyExpectation);
    }
    @SafeVarargs
    public FlowPropertyDefinitionBuilder(String name, Class<? extends Object> dataClass, Class<? extends Object>... collectionClasses) {
        this.name = name;
        this.dataClassDefinition = new DataClassDefinitionImpl(dataClass, collectionClasses);
    }
    public FlowPropertyDefinitionBuilder(Class<? extends Object> dataClass) {
        this(toPropertyName(dataClass), dataClass);
    }

    /**
     * @return {@link FlowPropertyExpectation} - only explicitly supplied fields are set.
     */
    public FlowPropertyExpectation toFlowPropertyExpectation() {
        return new FlowPropertyExpectationImpl(name, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction,
            flowPropertyValueProvider, flowPropertyValuePersister, flowPropertyValueChangeListeners, saveBack, autoCreate, getAlternates(),
            dataClassDefinition, this.propertiesDependentOn, this.initial);
    }

    /**
     * Used to generate the {@link FlowPropertyDefinition}
     *
     * @return {@link FlowPropertyExpectation} with all fields supplied a value ( using defaults as
     *         needed)
     */
    public FlowPropertyExpectation toCompleteFlowPropertyExpectation() {

        FlowActivityPhase propertyRequired = getPropertyRequired();
        if (propertyRequired == null) {
            propertyRequired = FlowActivityPhase.optional;
        }
        PropertyScope propertyScope = this.getPropertyScope();
        if (propertyScope == null) {
            propertyScope = PropertyScope.flowLocal;
        }

        FlowPropertyValuePersister flowPropertyValuePersister = this.getFlowPropertyValuePersister();
        ExternalPropertyAccessRestriction externalPropertyAccessRestriction = getExternalPropertyAccessRestriction();
        PropertyUsage propertyUsage = this.getPropertyUsage();
        if (propertyUsage == null) {
            // TODO: should default be different if there is a persister?
            // also what about externalPropertyAccessRestriction?
            // or should we just have simple defaults.
            propertyUsage = PropertyUsage.use;
        }
        if (externalPropertyAccessRestriction == null) {
            if (!propertyUsage.isOutputedProperty() && !propertyUsage.isExternallySettable()) {
                // not outputted nor set externally then by default internal
                externalPropertyAccessRestriction = ExternalPropertyAccessRestriction.noAccess;
            } else if (!propertyUsage.isExternallySettable()) {
                // is not allowed to set
                externalPropertyAccessRestriction = ExternalPropertyAccessRestriction.readonly;
            } else if (!propertyUsage.isOutputedProperty()) {
                // cannot read externally but can be set externally ( something like a password )
                externalPropertyAccessRestriction = ExternalPropertyAccessRestriction.writeonly;
            } else {
                // can be read/write externally
                externalPropertyAccessRestriction = ExternalPropertyAccessRestriction.noRestrictions;
            }
        }
        DataClassDefinitionImpl dataClassDefinition = this.getDataClassDefinition();
        if (dataClassDefinition == null) {
            dataClassDefinition = new DataClassDefinitionImpl();
        }

        if (propertyUsage.getAltersProperty() == Boolean.FALSE) {
            // read-only properties MUST not have a persister.
            flowPropertyValuePersister = null;
        }

        Boolean autoCreate = this.getAutoCreate();
        if (autoCreate == null) {
            // see note on #initAutoCreate()
            // also if propertyRequired = activate / then we shouldn't create the property
            // because it was expected to already be created.
            if (!dataClassDefinition.isCollection()) {
                Class<?> dataClass = dataClassDefinition.getDataClass();
                if (dataClass.isPrimitive()) {
                    autoCreate = true;
                } else if (dataClass.isInterface() || dataClass.isEnum()) {
                    autoCreate = false;
                }
            } else {
                // shouldn't a collection object be automatically created?
            }
        }
        Boolean saveBack = getSaveBack();
        if (saveBack == null) {
            saveBack = dataClassDefinition.isCollection() || JsonSelfRenderer.class.isAssignableFrom(dataClassDefinition.getDataClass());
        }
        return new FlowPropertyExpectationImpl(name, propertyRequired, propertyScope, propertyUsage, externalPropertyAccessRestriction,
            flowPropertyValueProvider, flowPropertyValuePersister, flowPropertyValueChangeListeners, saveBack, autoCreate, getAlternates(),
            dataClassDefinition, propertiesDependentOn, this.initial);
    }

    /**
     * The {@link #toCompleteFlowPropertyExpectation()} is used to create the
     * {@link FlowPropertyDefinition}.
     *
     * @return the created {@link FlowPropertyDefinitionImplementor} by the builder
     */
    public <FPD extends FlowPropertyDefinitionImplementor> FPD toFlowPropertyDefinition() {
        return toFlowPropertyDefinition(null);
    }

    /**
     * Only public access to the built {@link FlowPropertyDefinition}. As part of the final
     * construction:
     * <ol>
     * <li>any persisters to non-persistent objects are removed.
     * <li>any value providers to values that are expected to exist are removed.
     * </ol>
     *
     * @param flowTranslatorResolver
     * @return the generated FlowPropertyDefinitionImplementor
     */
    public <FPD extends FlowPropertyDefinitionImplementor> FPD toFlowPropertyDefinition(FlowTranslatorResolver flowTranslatorResolver) {
        FlowPropertyDefinitionImpl flowPropertyDefinition = new FlowPropertyDefinitionImpl(this.toCompleteFlowPropertyExpectation());
        if (flowTranslatorResolver != null) {
            flowTranslatorResolver.resolve(null, flowPropertyDefinition);
        }
        return (FPD) flowPropertyDefinition;
    }

    /**
     * just combines the lists.
     * Simple (could be replaced by generic utility - probably have one just done't want to look now)
     * @param additionalConfigurationParameters
     * @return
     */
    @SafeVarargs
    public static List<FlowPropertyExpectation> combine(List<FlowPropertyExpectation>... additionalConfigurationParameters) {
        List<FlowPropertyExpectation> results = new ArrayList<>();
        for(List<FlowPropertyExpectation> additionalConfigurationParameter: additionalConfigurationParameters) {
            results.addAll(additionalConfigurationParameter);
        }
        return results;
    }
    /**
     * used to apply a standard set of expectations to a specific property.
     * @param propertyName
     * @param additionalConfigurationParameters
     * @return a single condensed list of expectations.
     */
    @SafeVarargs
    public static List<FlowPropertyExpectation> merge(String propertyName, List<FlowPropertyExpectation>... additionalConfigurationParameters) {
        return merge(new FlowPropertyExpectationImpl(propertyName), additionalConfigurationParameters);
    }

    /**
     * create a new list of {@link FlowPropertyExpectation} that combine flowPropertyExpectation parameter with the default values the additionalConfigurationParameters lists.
     * @param flowPropertyExpectation
     * @param additionalConfigurationParameters
     * @return a single condensed list of expectations.
     */
    @SafeVarargs
    public static List<FlowPropertyExpectation> merge(FlowPropertyExpectation flowPropertyExpectation, List<FlowPropertyExpectation>... additionalConfigurationParameters) {
        List<FlowPropertyExpectation> results = new ArrayList<>();
        for(List<FlowPropertyExpectation> additionalConfigurationParameter: additionalConfigurationParameters) {
            for(FlowPropertyExpectation expectation: additionalConfigurationParameter) {
                if ( expectation.isApplicable(flowPropertyExpectation)) {
                    FlowPropertyExpectation propertyExpectation = new FlowPropertyExpectationImpl(flowPropertyExpectation, expectation);
                    results.add(propertyExpectation);
                }
            }
        }
        return results;
    }
    /**
     * scans through all the {@link FlowPropertyExpectation}s looking for expectations that
     * {@link FlowPropertyExpectation#isApplicable(FlowPropertyExpectation)} those
     * expectations have their values applied to flowPropertyDefinition in the order they are
     * encountered.
     *
     * @param additionalConfigurationParameters a list because order matters.
     * @return this
     */
    public FlowPropertyDefinitionBuilder applyFlowPropertyExpectations(List<FlowPropertyExpectation>... additionalConfigurationParameters) {
        for (List<FlowPropertyExpectation> additionalConfigurationParameterList : NotNullIterator.<List<FlowPropertyExpectation>> newNotNullIterator(additionalConfigurationParameters)) {
            for (FlowPropertyExpectation flowPropertyExpectation : NotNullIterator.<FlowPropertyExpectation> newNotNullIterator(additionalConfigurationParameterList)) {
                this.applyFlowPropertyExpectation(flowPropertyExpectation);
            }
        }

        return this;
    }

    /**
     * Copy over/merge flowPropertyExpectation with current information. The passed flowPropertyExpectation is favored over the current settings.
     * @param flowPropertyExpectation
     */
    public void applyFlowPropertyExpectation(FlowPropertyExpectation flowPropertyExpectation) {
        if ( isApplicable(flowPropertyExpectation)) {
            this.autoCreate = getFirstNonNull(flowPropertyExpectation.getAutoCreate(), this.autoCreate);
            this.addNames(flowPropertyExpectation.getAlternates());
            // should we do the merge?
            this.dataClassDefinition = getFirstNonNull(flowPropertyExpectation.getDataClassDefinition(), this.getDataClassDefinition());
            // this.defaultObject ( converted to FPVP )
            this.externalPropertyAccessRestriction =getFirstNonNull(flowPropertyExpectation.getExternalPropertyAccessRestriction(), this.getExternalPropertyAccessRestriction() );
            addAllIfNotContains(this.flowPropertyValueChangeListeners, flowPropertyExpectation.getFlowPropertyValueChangeListeners());
            this.flowPropertyValuePersister = getFirstNonNull(flowPropertyExpectation.getFlowPropertyValuePersister(), this.getFlowPropertyValuePersister());
            this.flowPropertyValueProvider = getFirstNonNull(flowPropertyExpectation.getFlowPropertyValueProvider(), this.getFlowPropertyValueProvider());
            addAllIfNotContains(this.getPropertiesDependentOn(), flowPropertyExpectation.getPropertiesDependentOn());
            this.propertyRequired = getFirstNonNull(flowPropertyExpectation.getPropertyRequired(), this.getPropertyRequired());
            this.propertyScope = getFirstNonNull(flowPropertyExpectation.getPropertyScope(), this.getPropertyScope());
            this.propertyUsage = getFirstNonNull(flowPropertyExpectation.getPropertyUsage(), this.getPropertyUsage());
            this.saveBack = getFirstNonNull(flowPropertyExpectation.getSaveBack(), this.getSaveBack());
            this.initial = getFirstNonNull(flowPropertyExpectation.getInitial(), this.getInitial());
        }
    }

    public ExternalPropertyAccessRestriction getExternalPropertyAccessRestriction() {
        return this.externalPropertyAccessRestriction;
    }
    public Object getDefaultObject() {
        return this.defaultObject;
    }
    public FlowPropertyDefinitionBuilder initFlowPropertyValuePersister(
        FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister) {
        _initFlowPropertyValuePersister(flowPropertyValuePersister, true);
        return this;
    }

    private boolean _initFlowPropertyValuePersister(FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister,
        boolean failOnNotHandling) {
        if (flowPropertyValuePersister.isHandling(this.toCompleteFlowPropertyExpectation())) {
            this.flowPropertyValuePersister = flowPropertyValuePersister;
            return true;
        } else if ( failOnNotHandling ){
            throw new FlowConfigurationException(flowPropertyValuePersister + " not handling " + this.name);
        } else {
            return false;
        }
    }
    /*
     * borrowed from FlowActivityImpl: we should have multiple FPVPs
     *
     * This handles the linking of {@link ChainedFlowPropertyValueProvider}.
     *
     * HACK ... should only be called from  {@link #addStandardFlowPropertyDefinitions()} NOT {@link #initializeFlow()}
     * this is because adding to standard properties will not happen correctly ( the {@link org.amplafi.flow.FlowTranslatorResolver} is
     * visible.
     *
     * TODO: make chaining more common
     *
     * HACK: should be part for the FlowPropertyDefinitionBuilder
    @SuppressWarnings("unchecked")
    protected void handleFlowPropertyValueProvider(String key, FlowPropertyValueProvider flowPropertyValueProvider) {
        FlowPropertyDefinitionImplementor flowPropertyDefinition = this.getFlowPropertyDefinition(key, false);
        if ( flowPropertyDefinition != null) {
            if ( flowPropertyValueProvider instanceof ChainedFlowPropertyValueProvider) {
                ((ChainedFlowPropertyValueProvider)flowPropertyValueProvider).setPrevious(flowPropertyDefinition.getFlowPropertyValueProvider());
            }
            flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
        }
        flowPropertyDefinition = this.getFlowPropertyDefinitionDefinedInFlow(key);
        if ( flowPropertyDefinition != null) {
            if ( flowPropertyValueProvider instanceof ChainedFlowPropertyValueProvider) {
                ((ChainedFlowPropertyValueProvider)flowPropertyValueProvider).setPrevious(flowPropertyDefinition.getFlowPropertyValueProvider());
            }
            flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
        }
    }
     */

    public FlowPropertyDefinitionBuilder initFlowPropertyValueProvider(
        FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
        return initFlowPropertyValueProvider(flowPropertyValueProvider, true);
    }

    public FlowPropertyDefinitionBuilder initFlowPropertyValueProvider(
        FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, boolean failOnNotHandling) {
        if (flowPropertyValueProvider.isHandling(this.toCompleteFlowPropertyExpectation())) {
            this.flowPropertyValueProvider = flowPropertyValueProvider;
        } else if ( failOnNotHandling ){
            throw new FlowConfigurationException(flowPropertyValueProvider + " not handling " + this.name);
        }
        return this;
    }

    public FlowPropertyDefinitionBuilder initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage) {
        this.initPropertyScope(propertyScope);
        this.initPropertyUsage(propertyUsage);
        return this;
    }

    public FlowPropertyDefinitionBuilder initSensitive() {
        this.initExternalPropertyAccessRestriction(ExternalPropertyAccessRestriction.noAccess);
        return this;
    }

    public FlowPropertyDefinitionBuilder initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage,
        ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this.initPropertyScope(propertyScope);
        this.initPropertyUsage(propertyUsage);
        this.initExternalPropertyAccessRestriction(externalPropertyAccessRestriction);
        return this;
    }

    public void mergeDataType(FlowPropertyExpectation source) {
        this.getDataClassDefinition().merge(source.getDataClassDefinition());
    }
    /**
     *
     * @param defaultProviders set of objects that may implement {@link FlowPropertyValuePersister}, {@link FlowPropertyValueProvider},
     * or {@link FlowPropertyValueChangeListener}. If the property being built is missing any of these helpers then the first defaultProvider is
     * assigned.
     * If the property is readonly ( {@link FlowPropertyDefinition#isReadOnly()} then no {@link FlowPropertyValuePersister} will be assigned.
     * @return a possibly different {@link FlowPropertyDefinitionBuilder}
     */
    public FlowPropertyDefinitionBuilder applyDefaultProviders(Object... defaultProviders) {
        boolean needPersister = this.getFlowPropertyValuePersister() == null;
        boolean needProvider = this.getFlowPropertyValueProvider() == null;
        for (Object provider : NotNullIterator.<Object> newNotNullIterator(defaultProviders)) {
            if (needPersister && (provider instanceof FlowPropertyValuePersister)) {
                needPersister = !_initFlowPropertyValuePersister((FlowPropertyValuePersister<FlowPropertyProvider>) provider, false);
            }
            if (needProvider && (provider instanceof FlowPropertyValueProvider)) {
                FlowPropertyValueProvider flowPropertyValueProvider = (FlowPropertyValueProvider) provider;
                if (flowPropertyValueProvider.isHandling(this.toCompleteFlowPropertyExpectation())) {
                    this.initFlowPropertyValueProvider(flowPropertyValueProvider);
                    needProvider = false;
                }
                //                needProvider= !_initFlowPropertyValueProvider((FlowPropertyValueProvider<? extends FlowPropertyProvider>)provider, false);
            }
            if (provider instanceof FlowPropertyValueChangeListener) {
                this.initFlowPropertyValueChangeListener((FlowPropertyValueChangeListener) provider);
            }
        }
        return this;
    }

    public FlowPropertyDefinitionBuilder initTranslator(FlowTranslator<?> flowTranslator) {
        this.dataClassDefinition.setFlowTranslator(flowTranslator);
        return this;
    }

    public FlowPropertyDefinitionBuilder initElementFlowTranslator(FlowTranslator<?> flowTranslator) {
        this.getDataClassDefinition().getElementDataClassDefinition().setFlowTranslator(flowTranslator);
        return this;
    }
    public FlowPropertyDefinitionBuilder initKeyFlowTranslator(FlowTranslator<?> flowTranslator) {
        this.getDataClassDefinition().getKeyDataClassDefinition().setFlowTranslator(flowTranslator);
        return this;
    }

    /**
     * @param flowActivityPhase the phase at which the property must be able to supply a value.
     * @return this
     */
    public FlowPropertyDefinitionBuilder initPropertyRequired(FlowActivityPhase flowActivityPhase) {
        this.propertyRequired= flowActivityPhase;
        return this;
    }

    public FlowPropertyDefinitionBuilder addPropertiesDependentOn(FlowPropertyExpectation... propertiesDependentOn) {
        if ( this.propertiesDependentOn == null) {
            this.propertiesDependentOn = new HashSet<FlowPropertyExpectation>();
        }
        addAllIfNotContains(this.getPropertiesDependentOn(),propertiesDependentOn);
        return this;
    }

    public FlowPropertyDefinitionBuilder initPropertyScope(PropertyScope propertyScope) {
        this.propertyScope = propertyScope;
        return this;
    }


    public PropertyScope getPropertyScope() {
        return propertyScope;
    }
    public FlowPropertyDefinitionBuilder initPropertyUsage(PropertyUsage propertyUsage) {
        this.propertyUsage = propertyUsage;
        return this;
    }

    /**
     * Convience wrapper to define a {@link FlowPropertyValueProvider} that returns a constant.
     * @param defaultObject
     * @return this
     */
    public FlowPropertyDefinitionBuilder initDefaultObject(Object defaultObject) {
        if ( defaultObject instanceof FlowPropertyValueProvider<?>) {
            this.initFlowPropertyValueProvider((FlowPropertyValueProvider<FlowPropertyProvider>)defaultObject);
        } else if ( defaultObject != null) {
            if (this.dataClassDefinition != null && this.dataClassDefinition.isDataClassDefined()) {
                // validate that the datatype previously defined is compatable
                if (!this.dataClassDefinition.getDataClass().isPrimitive()) {
                    // really need to handle the autobox issue better.
                    dataClassDefinition.getDataClass().cast(defaultObject);
                }
            } else {
                this.setDataClass(defaultObject.getClass());
            }

            this.initFlowPropertyValueProvider(new FixedFlowPropertyValueProvider(defaultObject));
        }
        return this;
    }
    /**
     * TODO: Note that anything that extends {@link CharSequence} is does not set the class
     * this was to allow configuration from xml where everything is a string ( we want to be able to convert strings to some other class)
     *
     * TODO: maybe this class ignoring to just strings?
     *
     * @param dataClass
     * @return this
     */
    public FlowPropertyDefinitionBuilder setDataClass(Class<? extends Object> dataClass) {
        if ( this.getDataClassDefinition() == null) {
            this.dataClassDefinition = new DataClassDefinitionImpl(dataClass);
        } else {
            this.getDataClassDefinition().setDataClass(dataClass);
        }
        return this;
    }

    public FlowPropertyDefinitionBuilder initExternalPropertyAccessRestriction(ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this.externalPropertyAccessRestriction = externalPropertyAccessRestriction;
        return this;
    }

    // problematic as DataClassDefinitionImpl could be altered.
    public DataClassDefinitionImpl getDataClassDefinition() {
        return this.dataClassDefinition;
    }

    public FlowPropertyValuePersister<FlowPropertyProvider> getFlowPropertyValuePersister() {
        return this.flowPropertyValuePersister;
    }

    public FlowPropertyValueProvider<? extends FlowPropertyProvider> getFlowPropertyValueProvider() {
        return this.flowPropertyValueProvider;
    }
    public boolean isReadOnly() {
        return getPropertyUsage().getAltersProperty() == Boolean.FALSE;
    }

    public boolean isApplicable(FlowPropertyExpectation flowPropertyExpectation) {
        return getName() == null || flowPropertyExpectation.getName() == null || flowPropertyExpectation.isNamed(getName());
    }

    public String getName() {
        return name;
    }

    public PropertyUsage getPropertyUsage() {
        return this.propertyUsage;
    }
    public static String toPropertyName(Class<?> clazz) {
        if ( clazz.isPrimitive() ) {
            throw new FlowConfigurationException(clazz+ ": must supply property name if clazz is a primitive");
        }
        String packageName = clazz.getPackage().getName();
        if(packageName.startsWith("java.") || packageName.startsWith("javax.")) {
            throw new FlowConfigurationException( clazz+ ": must supply property name if clazz is java[x] class");
        }
        return propertyNameFromClassName.get(clazz);
    }

    /**
     * Sets autoCreate to true - meaning that if the property does not exist in
     * the cache, a new instance is created. This avoids a property returning null.
     * Only needed if the property does not have a {@link #initFlowPropertyValueProvider(FlowPropertyValueProvider)}
     * set OR the {@link FlowPropertyValueProvider} may return null.
     *
     * @deprecated replace with list of {@link FlowPropertyValueProvider} concept AND {@link #initDefaultObject(Object)}
     * where there is standard defaults.
     * @return this
     */
    @Deprecated
    public FlowPropertyDefinitionBuilder initAutoCreate() {
        this.autoCreate = true;
        return this;
    }

    public Boolean getAutoCreate() {
        return this.autoCreate;
    }
    public FlowPropertyDefinitionBuilder addNames(String... alternateNames) {
        if ( this.getAlternates() == null) {
            this.alternates = new HashSet<String>();
        }
        addAllIfNotContains(this.getAlternates(), alternateNames);
        if ( this.getAlternates().isEmpty()) {
            this.alternates = null;
        }
        return this;
    }
    public FlowPropertyDefinitionBuilder addNames(Collection<String> alternateNames) {
        if ( this.getAlternates() == null) {
            this.alternates = new HashSet<String>();
        }
        addAllIfNotContains(this.getAlternates(), alternateNames);
        if ( this.getAlternates().isEmpty()) {
            this.alternates = null;
        }
        return this;
    }

    public Set<String> getAlternates() {
        return alternates;
    }
    public FlowPropertyDefinitionBuilder list(Class<?> elementClass) {
        this.dataClassDefinition = new DataClassDefinitionImpl(elementClass, List.class);
        return this;
    }
    public FlowPropertyDefinitionBuilder set(Class<?> elementClass) {
        this.dataClassDefinition = new DataClassDefinitionImpl(elementClass, Set.class);
        return this;
    }
    /**
     * Handles case where the key class is a string.
     * @param elementClass
     * @return this
     */
    public FlowPropertyDefinitionBuilder map(Class<?> elementClass) {
        return this.map(String.class, elementClass);
    }
    public FlowPropertyDefinitionBuilder map(Class<?> keyClass, Class<?> elementClass, Class<?>... elementCollectionClasses) {
        this.dataClassDefinition = DataClassDefinitionImpl.map(keyClass, elementClass, elementCollectionClasses);
        return this;
    }
    public FlowPropertyDefinitionBuilder initSaveBack(Boolean saveBack) {
        this.saveBack = saveBack;
        return this;
    }

    /**
     * Applies {@link #READ_ONLY_VALUE} to the property
     * @return this
     */
    public FlowPropertyDefinitionBuilder readOnly() {
        this.applyFlowPropertyExpectations(READ_ONLY_VALUE);
        return this;
    }
    /**
     * sets {@link #INTERNAL_ONLY} - {@link PropertyUsage#internalState} and {@link ExternalPropertyAccessRestriction#noAccess}
     * @return this
     */
    public FlowPropertyDefinitionBuilder internalOnly() {
        this.applyFlowPropertyExpectations(INTERNAL_ONLY);
        return this;
    }
    /**
     * applies {@link #API_RETURN_VALUE} to the property
     * @return this
     */
    public FlowPropertyDefinitionBuilder returned() {
        this.applyFlowPropertyExpectations(API_RETURN_VALUE);
        return this;
    }
    public FlowPropertyDefinitionBuilder initFlowPropertyValueChangeListener(FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        if ( flowPropertyValueChangeListener != null) {
            if ( this.flowPropertyValueChangeListeners == null) {
                this.flowPropertyValueChangeListeners = new ArrayList<>();
            }
            this.flowPropertyValueChangeListeners.add(flowPropertyValueChangeListener);
        }
        return this;
    }

    public List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners() {
        return flowPropertyValueChangeListeners;
    }
    //
//    /**
//     * called in {@link org.amplafi.flow.impl.FlowActivityImpl#addStandardFlowPropertyDefinitions} or a {@link org.amplafi.flow.impl.FlowActivityImpl} subclass's method.
//     * @param flowPropertyProvider
//     */
//
//    public final void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider) {
//        this.defineFlowPropertyDefinitions(flowPropertyProvider, null);
//    }
//
//    /**
//     * @param flowPropertyProvider
//     * @param additionalConfigurationParameters - a list because we want consistent fixed order that the additionalConfigurationParameters are applied.
//     */
//
//    public void defineFlowPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<FlowPropertyExpectation> additionalConfigurationParameters) {
//        // we copy the FlowPropertyDefinitionBuilder so that the original can be reused.
//        FlowPropertyDefinitionImplementor returnedFlowPropertyDefinition = this.initPropertyDefinition(flowPropertyProvider, new FlowPropertyDefinitionBuilder(this), additionalConfigurationParameters);
//        flowPropertyProvider.addPropertyDefinitions(returnedFlowPropertyDefinition);
//    }
//    /**
//     * initialize a flowPropertyDefinition.
//     * @param flowPropertyProvider
//     * @param flowPropertyDefinitionBuilder will be modified (make sure not modifying the master definition)
//     * @param additionalConfigurationParameters
//     */
//    // BROKEN : borrowed code from AbstractFlowPropertyDefinitionProvider ( bad assumptions about this )
//    protected FlowPropertyDefinitionImplementor initPropertyDefinition(
//        FlowPropertyProviderImplementor flowPropertyProvider,
//        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder, List<FlowPropertyExpectation> additionalConfigurationParameters) {
//        flowPropertyDefinitionBuilder = flowPropertyDefinitionBuilder
//            .applyFlowPropertyExpectations(additionalConfigurationParameters)
//            .applyDefaultProviders(flowPropertyProvider); // BROKEN ( 'this' is not a FPVP ...)
//        FlowPropertyDefinitionImplementor returnedFlowPropertyDefinition = flowPropertyDefinitionBuilder.toFlowPropertyDefinition();
//        // TODO : also create a "read-only" v. writeable property mechanism
//        // TODO feels like it should be part of an 'FlowPropertyDefinitionBuilder.apply()' method
//        // should look for other helpers that need opportunity to get their definitions
//        if ( !returnedFlowPropertyDefinition.isReadOnly()) {
//            // only set persisters on non-read-only objects.
//            FlowPropertyValuePersister<?> flowPropertyValuePersister = returnedFlowPropertyDefinition.getFlowPropertyValuePersister();
//            // BROKEN ( 'this' is not a FPVP ...)
//            if ( flowPropertyValuePersister instanceof FlowPropertyDefinitionProvider /*&& flowPropertyValuePersister != this*/){
//                // TODO: note: infinite loop possibilities here if 2 different objects have mutually dependent FPDs
//                ((FlowPropertyDefinitionProvider)flowPropertyValuePersister).defineFlowPropertyDefinitions(flowPropertyProvider, additionalConfigurationParameters);
//            }
//        }
//        return returnedFlowPropertyDefinition;
//    }
//
//    public FlowPropertyDefinitionBuilder getFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass) {
//        if (dataClass != null && !this.flowPropertyDefinition.isAssignableFrom(dataClass)) {
//            return null;
//        } else {
//            return new FlowPropertyDefinitionBuilder(this.flowPropertyDefinition);
//        }
//    }
//
//    public List<String> getOutputFlowPropertyDefinitionNames() {
//        return Arrays.asList(this.flowPropertyDefinition.getName());
//    }

    public FlowActivityPhase getPropertyRequired() {
        return propertyRequired;
    }

    public Set<FlowPropertyExpectation> getPropertiesDependentOn() {
        return propertiesDependentOn;
    }

    public Boolean getSaveBack() {
        return saveBack;
    }
    public FlowPropertyDefinitionBuilder setInitial(String initial) {
        this.initial = initial;
        return this;
    }
    public String getInitial() {
        return this.initial;
    }
    public boolean isAssignableFrom(Class<?> dataClass) {
        return this.dataClassDefinition == null || this.dataClassDefinition.isAssignableFrom(dataClass);
    }
    public boolean isOutputedProperty() {
        if ( this.propertyUsage != null) {
            return this.propertyUsage.isOutputedProperty();
        } else {
            // calculate current defaults
            return this.toCompleteFlowPropertyExpectation().getPropertyUsage().isOutputedProperty();
        }
    }
    @Override
    public String toString() {
        ToStringBuilder toStringBuilder = new ToStringBuilder(this);
        return toStringBuilder.toString();
    }
}
