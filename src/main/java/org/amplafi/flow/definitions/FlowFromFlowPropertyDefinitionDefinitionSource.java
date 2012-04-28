package org.amplafi.flow.definitions;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowPropertyDefinition;

import static org.amplafi.flow.FlowConstants.FSSINGLE_PROPERTY_NAME;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
import org.amplafi.flow.flowproperty.FlowPropertyExpectationImpl;
import org.amplafi.flow.flowproperty.PropertyUsage;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowImpl;

import com.sworddance.util.NotNullIterator;

import static com.sworddance.util.CUtilities.*;
import static org.amplafi.flow.flowproperty.PropertyScope.flowLocal;
import static org.amplafi.flow.flowproperty.PropertyUsage.internalState;

/**
 * Define flows only needed to provide a property that is used by the ui of another flow. but is not needed by the execution of the flow itself.
 * @author patmoore
 *
 */
public class FlowFromFlowPropertyDefinitionDefinitionSource implements DefinitionSource<FlowImplementor> {

    private final Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();

    public FlowFromFlowPropertyDefinitionDefinitionSource() {

    }
    public FlowFromFlowPropertyDefinitionDefinitionSource(FlowPropertyDefinitionImplementor...flowPropertyDefinitionImplementors) {
        for(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor: NotNullIterator.<FlowPropertyDefinitionImplementor>newNotNullIterator(flowPropertyDefinitionImplementors)) {
            String flowPropertyName = flowPropertyDefinitionImplementor.getName();
            FlowImpl flow = new FlowImpl(flowPropertyName+"Flow");
            flow.addPropertyDefinition(new FlowPropertyDefinitionImpl(FSSINGLE_PROPERTY_NAME).initAccess(flowLocal, internalState).initDefaultObject(flowPropertyName));
            FlowActivityImpl flowActivity = new FlowActivityImpl(flowPropertyName+"FlowActivity");
            flowActivity.addPropertyDefinition(flowPropertyDefinitionImplementor);
            flow.addActivity(flowActivity);
            put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
        }
    }

    // HACK having to pass in the property name seems weak.
    public void add(String flowPropertyName, FlowPropertyDefinitionProvider flowPropertyDefinitionProvider) {
        FlowImpl flow = new FlowImpl(flowPropertyName+"Flow");
        FlowActivityImpl flowActivity = new FlowActivityImpl("FA1");
        flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity);
        flow.addActivity(flowActivity);
        put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
    }
    /**
     * Add {@link FlowPropertyDefinition}s from the flowPropertyDefinitionProviders as a flow for each property.
     *
     * Only the properties that have {@link FlowPropertyDefinition#getPropertyUsage()}.{@link PropertyUsage#isOutputedProperty()} != false get their own flow.
     * This makes it safe to list properties that are expected on input without having a flow created for those input only properties.
     *
     * @param flowPropertyDefinitionProviders
     */
    public void add(FlowPropertyDefinitionProvider... flowPropertyDefinitionProviders) {
        for(FlowPropertyDefinitionProvider flowPropertyDefinitionProvider :flowPropertyDefinitionProviders) {
            for(String flowPropertyName : flowPropertyDefinitionProvider.getOutputFlowPropertyDefinitionNames()) {
                FlowImpl flow = new FlowImpl(flowPropertyName+"Flow");
                flow.addPropertyDefinition(new FlowPropertyDefinitionImpl(FlowConstants.FSSINGLE_PROPERTY_NAME).initAccess(flowLocal, internalState).initDefaultObject(flowPropertyName));
                FlowActivityImpl flowActivity = new FlowActivityImpl("FA1");
                flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity, Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.finish)));
                flow.addActivity(flowActivity);
                put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
            }
        }
    }
    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinition(java.lang.String)
     */
    @Override
    public FlowImplementor getFlowDefinition(String flowTypeName) {
        return this.flows.get(flowTypeName);
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinitions()
     */
    @Override
    public Map<String, FlowImplementor> getFlowDefinitions() {
        return this.flows;
    }

    /**
     * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
     */
    @Override
    public boolean isFlowDefined(String flowTypeName) {
        return this.flows.containsKey(flowTypeName);
    }
}
