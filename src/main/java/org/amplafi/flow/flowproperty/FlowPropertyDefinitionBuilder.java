package org.amplafi.flow.flowproperty;

import java.util.Arrays;
import java.util.List;

import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.NotNullIterator;

/**
 * Designed to handle the problems of extending standard definitions.
 *
 * TODO: ? add a way to make an attempt to read an unset property fail rather than return null?
 * Useful for {@link PropertyUsage#getSetsValue()} == TRUE
 * TODO: should really be a builder ( 1 per FPD )
 * TODO: remove initDefaultObject()
 * Handles some common use cases
 * @author patmoore
 *
 */
public class FlowPropertyDefinitionBuilder {

	private FlowPropertyDefinitionImplementor flowPropertyDefinition;
	public FlowPropertyDefinitionBuilder() {

	}
	public FlowPropertyDefinitionBuilder(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor) {
		this.flowPropertyDefinition = flowPropertyDefinitionImplementor;
	}
    /**
     * A property that is not allowed to be altered. (no set is allowed) But the property is not immutable because a FPVP could supply different values.
     * Use case: User id
     *
     * Specifically:
     * PropertyScope.flowLocal,
     * PropertyUsage.initialize,
     * ExternalPropertyAccessRestriction.readonly
     *
     * @param name
     * @param dataClass
     * @param collectionClasses
     * @return this
     */
    @SuppressWarnings("unchecked")
    public static FlowPropertyDefinitionBuilder createNonalterableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowPropertyValueProvider flowPropertyValueProvider, Class<?>...collectionClasses) {
        FlowPropertyDefinitionImpl flowPropertyDefinitionImplementor = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses);
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = new FlowPropertyDefinitionBuilder(flowPropertyDefinitionImplementor).
        initAccess(PropertyScope.flowLocal, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly).initFlowPropertyValueProvider(flowPropertyValueProvider);
        return flowPropertyDefinitionBuilder;
    }
    /**
     * Expectation is that {@link FlowPropertyValueProvider} will be supplied later.
     * @param name
     * @param dataClass
     * @param whenMustBeAvailable
     * @param collectionClasses
     * @return this
     */
    public FlowPropertyDefinitionBuilder createNonalterableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowActivityPhase whenMustBeAvailable, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, whenMustBeAvailable, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly);
        return this;
    }
    public FlowPropertyDefinitionBuilder createImmutableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Object immutableValue, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.initialize, ExternalPropertyAccessRestriction.readonly).initDefaultObject(immutableValue);
        return this;
    }
    /**
     * A property that cannot be configured by the initialFlowState map, but is alterable by a {@link FlowPropertyValueProvider}
     * and can be changed by the user during the flow.
     *
     * This property is guaranteed to have a value if the flow completes normally.
     *
     * {@link #createInternalStateFlowPropertyDefinitionWithDefault(String, Class, Class...)} to create a property that is not
     * visible for exporting.
     *
     * @param name
     * @param dataClass
     * @param flowPropertyValueProvider
     * @param collectionClasses
     * @return this
     */
    public FlowPropertyDefinitionBuilder createNonconfigurableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowPropertyValueProvider flowPropertyValueProvider, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.initialize).initFlowPropertyValueProvider(flowPropertyValueProvider);
        return this;
    }
    /**
     * A security property
     * Use case: a password
     * @param name
     * @param dataClass
     * @param collectionClasses
     * @return this
     */
    public FlowPropertyDefinitionBuilder createPasswordFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, FlowActivityPhase.activate, collectionClasses).
            initAccess(PropertyScope.flowLocal, PropertyUsage.consume, ExternalPropertyAccessRestriction.writeonly);
        return this;
    }

    /**
     * Used to create a value that must be available by the time the flow completes.
     * @param name
     * @param dataClass
     * @param collectionClasses
     * @return this
     */
    public FlowPropertyDefinitionBuilder createApiReturnValueFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, FlowActivityPhase.finish, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.suppliesIfMissing);
        return this;
    }
    /**
     * create a {@link FlowPropertyDefinition} for a property whose value is recomputed for every request.
     *
     * Use case: very dynamic properties for example, a status message.
     * @param name
     * @param dataClass
     * @param collectionClasses
     * @return this
     */
    public FlowPropertyDefinitionBuilder createCurrentRequestOnlyFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, FlowActivityPhase.optional, collectionClasses).
        initAccess(PropertyScope.requestFlowLocal, PropertyUsage.suppliesIfMissing);
        return this;
    }
    /**
     * A flow property for a property that will be created by the flow.
     *
     * Use case:
     * create a new user.
     * @param name
     * @param dataClass
     * @param collectionClasses
     * @return this
     */
    public FlowPropertyDefinitionBuilder createCreatingFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowActivityPhase whenCreated, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, whenCreated, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.suppliesIfMissing);
        return this;
    }

	public FlowPropertyDefinitionBuilder createFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, collectionClasses);
        return this;
    }

    public FlowPropertyDefinitionBuilder createFlowPropertyDefinitionWithDefault(String name, Class<? extends Object> dataClass, Object defaultObject, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.io).initDefaultObject(defaultObject);
        return this;
    }

    public FlowPropertyDefinitionBuilder createInternalStateFlowPropertyDefinitionWithDefault(String name, Class<? extends Object> dataClass, Class<?>...collectionClasses) {
    	return createInternalStateFlowPropertyDefinitionWithDefault(name, dataClass, PropertyScope.flowLocal, collectionClasses);
    }
    public FlowPropertyDefinitionBuilder createInternalStateFlowPropertyDefinitionWithDefault(String name, Class<? extends Object> dataClass, PropertyScope propertyScope, Class<?>...collectionClasses) {
        ApplicationIllegalArgumentException.valid(propertyScope != PropertyScope.global, "internalState cannot be global");
        this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).
        initAccess(propertyScope, PropertyUsage.internalState);
        return this;
    }
    @SuppressWarnings("unchecked")
    public <FE extends FlowPropertyExpectation> FE createFlowPropertyExpectation(FlowPropertyDefinition flowPropertyDefinition, FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        return (FE) new FlowPropertyExpectationImpl(flowPropertyDefinition.getName(), flowPropertyValueChangeListener);
    }

    /**
     * Use case :
     * Create a new User
     *
     * Which should be used? {@link #createCreatingFlowPropertyDefinition(String, Class, FlowActivityPhase, Class...)} ?
     * @param <FE>
     * @param flowPropertyDefinition
     * @param flowPropertyValueProvider
     * @param flowPropertyValuePersister
     * @return
     */
    public <FE extends FlowPropertyExpectation> FE createFlowPropertyExpectationToCreateReadOnlyFlowPropertyDefinition(FlowPropertyDefinition flowPropertyDefinition, FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, FlowPropertyValuePersister flowPropertyValuePersister) {
        return (FE) new FlowPropertyExpectationImpl(flowPropertyDefinition.getName(), null, null, null, null, flowPropertyValueProvider, flowPropertyValuePersister, null);
    }
    /**
     * scans through all the {@link FlowPropertyExpectation}s looking for expectations that {@link FlowPropertyExpectation#isApplicable(FlowPropertyDefinitionImplementor)}
     * those expectations have their values applied to flowPropertyDefinition in the order they are encountered.
     * @param additionalConfigurationParameters a list because order matters.
     * @return this
     */
    public FlowPropertyDefinitionBuilder applyFlowPropertyExpectations(List<FlowPropertyExpectation> additionalConfigurationParameters) {
        for(FlowPropertyExpectation flowPropertyExpectation: NotNullIterator.<FlowPropertyExpectation>newNotNullIterator(additionalConfigurationParameters)) {
            if(flowPropertyExpectation.isApplicable(flowPropertyDefinition)) {
                // FlowPropertyExpectation expectation applies

            	// TODO: use init so that if the flowPropertyDefinition already has a different value then a new flowPD can be created.
                flowPropertyDefinition.addFlowPropertyValueChangeListeners(flowPropertyExpectation.getFlowPropertyValueChangeListeners());
                FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider = flowPropertyExpectation.getFlowPropertyValueProvider();
                if ( flowPropertyValueProvider != null) {
                    // forces a new valueProvider
                    initFlowPropertyValueProvider(flowPropertyValueProvider);
                }
                FlowPropertyValuePersister flowPropertyValuePersister = flowPropertyExpectation.getFlowPropertyValuePersister();
                if ( flowPropertyValuePersister != null) {
                    // forces a new flowPropertyValuePersister
                	initFlowPropertyValuePersister(flowPropertyValuePersister);
                }

                // TODO: Need to do test and clone!!
                FlowActivityPhase flowActivityPhase = flowPropertyExpectation.getPropertyRequired();
                if ( flowActivityPhase != null ) {
                    flowPropertyDefinition = flowPropertyDefinition.initPropertyRequired(flowActivityPhase);
                }
                PropertyScope propertyScope = flowPropertyExpectation.getPropertyScope();
                if ( propertyScope != null) {
                    flowPropertyDefinition = flowPropertyDefinition.initPropertyScope(propertyScope);
                }
                PropertyUsage propertyUsage = flowPropertyExpectation.getPropertyUsage();
                if ( propertyUsage != null ) {
                    flowPropertyDefinition = flowPropertyDefinition.initPropertyUsage(propertyUsage);
                }
            }
        }
        return this;
    }
	public FlowPropertyDefinitionBuilder initFlowPropertyValuePersister(FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister) {
		return initFlowPropertyValuePersister(flowPropertyValuePersister, true);
	}
	public FlowPropertyDefinitionBuilder initFlowPropertyValuePersister(FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister, boolean failOnNotHandling) {
		_initFlowPropertyValuePersister(flowPropertyValuePersister, failOnNotHandling);
		return this;
	}
	private boolean _initFlowPropertyValuePersister(FlowPropertyValuePersister<? extends FlowPropertyProvider> flowPropertyValuePersister, boolean failOnNotHandling) {
		if ( flowPropertyValuePersister.isHandling(flowPropertyDefinition)) {
			this.flowPropertyDefinition = this.flowPropertyDefinition.initFlowPropertyValuePersister(flowPropertyValuePersister);
			return true;
		} else {
			ApplicationIllegalArgumentException.valid(!failOnNotHandling, flowPropertyValuePersister+" not handling "+this.flowPropertyDefinition);
			return false;
		}
	}
	public FlowPropertyDefinitionBuilder initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
		return initFlowPropertyValueProvider(flowPropertyValueProvider, true);
	}
	public FlowPropertyDefinitionBuilder initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, boolean failOnNotHandling) {
		_initFlowPropertyValueProvider(flowPropertyValueProvider, failOnNotHandling);
		return this;
	}
	private boolean _initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, boolean failOnNotHandling) {
		if ( flowPropertyValueProvider.isHandling(flowPropertyDefinition)) {
			this.flowPropertyDefinition = this.flowPropertyDefinition.initFlowPropertyValueProvider(flowPropertyValueProvider);
			return true;
		} else {
			ApplicationIllegalArgumentException.valid(!failOnNotHandling, flowPropertyValueProvider+" not handling "+this.flowPropertyDefinition);
			return false;
		}
	}
    public FlowPropertyDefinitionBuilder initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage) {
    	this.flowPropertyDefinition = this.flowPropertyDefinition.initAccess(propertyScope, propertyUsage);
		return this;
	}
    public FlowPropertyDefinitionBuilder initAccess(PropertyScope propertyScope, PropertyUsage propertyUsage, ExternalPropertyAccessRestriction externalPropertyAccessRestriction) {
        this.flowPropertyDefinition = this.flowPropertyDefinition.initAccess(propertyScope, propertyUsage, externalPropertyAccessRestriction);
        return this;
    }
    public FlowPropertyDefinitionBuilder applyDefaultProviders(Object... defaultProviders) {
    	boolean needPersister = flowPropertyDefinition.getFlowPropertyValuePersister() == null && !flowPropertyDefinition.isCacheOnly();
    	boolean needProvider = !flowPropertyDefinition.isDefaultAvailable();
    	for(Object provider : NotNullIterator.<Object>newNotNullIterator(defaultProviders)) {
    		if (needPersister && (provider instanceof FlowPropertyValuePersister )) {
    			needPersister= !_initFlowPropertyValuePersister((FlowPropertyValuePersister<FlowPropertyProvider>)provider, false);
    		}
    		if ( needProvider && (provider instanceof FlowPropertyValueProvider)) {
    			needProvider= !_initFlowPropertyValueProvider((FlowPropertyValueProvider<? extends FlowPropertyProvider>)provider, false);
    		}
    		if ( provider instanceof FlowPropertyValueChangeListener) {
    			flowPropertyDefinition.addFlowPropertyValueChangeListeners(Arrays.asList((FlowPropertyValueChangeListener)provider));
    		}
    	}
		return this;
    }
    public <FPD extends FlowPropertyDefinitionImplementor> FPD toFlowPropertyDefinition() {
    	return (FPD) this.flowPropertyDefinition;
    }
    public FlowPropertyDefinitionBuilder initPropertyRequired(FlowActivityPhase flowActivityPhase) {
        this.flowPropertyDefinition = this.flowPropertyDefinition.initPropertyRequired(flowActivityPhase);
        return this;
    }
}
