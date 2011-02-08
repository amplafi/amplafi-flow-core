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
     * @param name
     * @param dataClass
     * @param collectionClasses
     * @return
     */
    @SuppressWarnings("unchecked")
    public FlowPropertyDefinitionBuilder createNonalterableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowPropertyValueProvider flowPropertyValueProvider, Class<?>...collectionClasses) {
        this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.initialize, PropertySecurity.readonly).initFlowPropertyValueProvider(flowPropertyValueProvider);
        return this;
    }
    /**
     * Expectation is that {@link FlowPropertyValueProvider} will be supplied later.
     * @param name
     * @param dataClass
     * @param whenMustBeAvailable
     * @param collectionClasses
     * @return
     */
    public FlowPropertyDefinitionBuilder createNonalterableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowActivityPhase whenMustBeAvailable, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, whenMustBeAvailable, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.initialize, PropertySecurity.readonly);
        return this;
    }
    public FlowPropertyDefinitionBuilder createImmutableFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Object immutableValue, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.initialize, PropertySecurity.readonly).initDefaultObject(immutableValue);
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
     * @return
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
     * @return
     */
    @SuppressWarnings("unchecked")
    public FlowPropertyDefinitionBuilder createPasswordFlowPropertyDefinition(String name, Class<? extends Object> dataClass, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, FlowActivityPhase.activate, collectionClasses).
            initAccess(PropertyScope.flowLocal, PropertyUsage.consume, PropertySecurity.writeonly);
        return this;
    }

    /**
     * Used to create a value that must be available by the time the flow completes.
     * @param name
     * @param dataClass
     * @param collectionClasses
     * @return
     */
    @SuppressWarnings("unchecked")
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
     * @return
     */
    @SuppressWarnings("unchecked")
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
     * @return
     */
    @SuppressWarnings("unchecked")
    public FlowPropertyDefinitionBuilder createCreatingFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowActivityPhase whenCreated, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, whenCreated, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.suppliesIfMissing);
        return this;
    }

    @SuppressWarnings("unchecked")
    public FlowPropertyDefinitionBuilder createFlowPropertyDefinition(String name, Class<? extends Object> dataClass, FlowActivityPhase whenRequired, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, whenRequired, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.io);
        return this;
    }
    @SuppressWarnings("unchecked")
    public FlowPropertyDefinitionBuilder createFlowPropertyDefinitionWithDefault(String name, Class<? extends Object> dataClass, Object defaultObject, Class<?>...collectionClasses) {
    	this.flowPropertyDefinition = new FlowPropertyDefinitionImpl(name, dataClass, null, collectionClasses).
        initAccess(PropertyScope.flowLocal, PropertyUsage.io).initDefaultObject(defaultObject);
        return this;
    }

    @SuppressWarnings("unchecked")
    public FlowPropertyDefinitionBuilder createInternalStateFlowPropertyDefinitionWithDefault(String name, Class<? extends Object> dataClass, Class<?>...collectionClasses) {
    	return createInternalStateFlowPropertyDefinitionWithDefault(name, dataClass, PropertyScope.flowLocal, collectionClasses);
    }
    @SuppressWarnings("unchecked")
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
     * @param flowPropertyDefinition TODO: should be part of the builder.
     * @param additionalConfigurationParameters a list because order matters.
     */
    public FlowPropertyDefinitionBuilder applyFlowPropertyExpectations(
            List<FlowPropertyExpectation> additionalConfigurationParameters) {
        for(FlowPropertyExpectation flowPropertyExpectation: NotNullIterator.<FlowPropertyExpectation>newNotNullIterator(additionalConfigurationParameters)) {
            if(flowPropertyExpectation.isApplicable(flowPropertyDefinition)) {
                // FlowPropertyExpectation expectation applies

            	// TODO: use init so that if the flowPropertyDefinition already has a different value then a new flowPD can be created.
                flowPropertyDefinition.addFlowPropertyValueChangeListeners(flowPropertyExpectation.getFlowPropertyValueChangeListeners());
                FlowPropertyValueProvider<FlowPropertyProvider> flowPropertyValueProvider = flowPropertyExpectation.getFlowPropertyValueProvider();
                if ( flowPropertyValueProvider != null) {
                    // forces a new valueProvider
                    flowPropertyDefinition.setFlowPropertyValueProvider(flowPropertyValueProvider);
                }
                FlowPropertyValuePersister flowPropertyValuePersister = flowPropertyExpectation.getFlowPropertyValuePersister();
                if ( flowPropertyValuePersister != null) {
                    // forces a new flowPropertyValuePersister
                    flowPropertyDefinition.setFlowPropertyValuePersister(flowPropertyValuePersister);
                }
                FlowActivityPhase flowActivityPhase = flowPropertyExpectation.getPropertyRequired();
                if ( flowActivityPhase != null ) {
                	flowPropertyDefinition.setPropertyRequired(flowActivityPhase);
                }
                PropertyScope propertyScope = flowPropertyExpectation.getPropertyScope();
                if ( propertyScope != null) {
                	flowPropertyDefinition.setPropertyScope(propertyScope);
                }
                PropertyUsage propertyUsage = flowPropertyExpectation.getPropertyUsage();
                if ( propertyUsage != null ) {
                	flowPropertyDefinition.setPropertyScope(propertyScope);
                }
            }
        }
        return this;
    }
	public FlowPropertyDefinitionBuilder initFlowPropertyValueProvider(FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider) {
		this.flowPropertyDefinition.initFlowPropertyValueProvider(flowPropertyValueProvider);
		return this;
	}
    public void applyDefaultProviders(Object... defaultProviders) {
    	boolean needPersister = flowPropertyDefinition.getFlowPropertyValuePersister() == null;
    	boolean needProvider = flowPropertyDefinition.getFlowPropertyValueProvider() == null;
    	for(Object provider : NotNullIterator.<Object>newNotNullIterator(defaultProviders)) {
    		if (needPersister && (provider instanceof FlowPropertyValuePersister )) {
    			flowPropertyDefinition = flowPropertyDefinition.initFlowPropertyValuePersister((FlowPropertyValuePersister<FlowPropertyProvider>)provider);
    			needPersister = false;
    		}
    		if ( needProvider && (provider instanceof FlowPropertyValueProvider)) {
    			flowPropertyDefinition = flowPropertyDefinition.initFlowPropertyValueProvider((FlowPropertyValueProvider<FlowPropertyProvider>)provider);
    			needProvider = false;
    		}
    		if ( provider instanceof FlowPropertyValueChangeListener) {
    			flowPropertyDefinition.addFlowPropertyValueChangeListeners(Arrays.asList((FlowPropertyValueChangeListener)provider));
    		}
    	}
    }
    public <FPD extends FlowPropertyDefinitionImplementor> FPD toFlowPropertyDefinition() {
    	return (FPD) this.flowPropertyDefinition;
    }

}
