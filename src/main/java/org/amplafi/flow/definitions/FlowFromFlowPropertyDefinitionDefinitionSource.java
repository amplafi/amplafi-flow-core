package org.amplafi.flow.definitions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowActivityPhase;
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
import org.apache.commons.lang.StringUtils;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.NotNullIterator;

import static com.sworddance.util.CUtilities.*;
import static org.amplafi.flow.flowproperty.PropertyScope.flowLocal;
import static org.amplafi.flow.flowproperty.PropertyUsage.internalState;

/**
 * Define flows only needed to provide a property that is used by the ui of another flow. but is not needed by the execution of the flow itself.
 *
 * TODO: looks like we should be able to refactor some of these methods that create the flow. Bothered by the lack of DRY.
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
            FlowImpl flow = createFlow(flowPropertyName);
            FlowActivityImpl flowActivity = new FlowActivityImpl("FA");
            flowActivity.addPropertyDefinition(flowPropertyDefinitionImplementor);
            flow.addActivity(flowActivity);
            put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
        }
    }

    // HACK having to pass in the property name seems weak.
    public void add(String flowPropertyName, FlowPropertyDefinitionProvider flowPropertyDefinitionProvider, List<FlowPropertyExpectation>additionalConfigurationParameters) {
        FlowImpl flow = createFlow(flowPropertyName);
        FlowActivityImpl flowActivity = new FlowActivityImpl("FA");
        flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity, additionalConfigurationParameters);
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
            List<String> outputFlowPropertyDefinitionNames = flowPropertyDefinitionProvider.getOutputFlowPropertyDefinitionNames();
            ApplicationIllegalArgumentException.valid(isNotEmpty(outputFlowPropertyDefinitionNames), flowPropertyDefinitionProvider.getClass(), " has no output properties defined.");
            for(String flowPropertyName : outputFlowPropertyDefinitionNames) {
                FlowImpl flow = createFlow(flowPropertyName);
                FlowActivityImpl flowActivity = new FlowActivityImpl("FA");
                // make sure the property being returned has a value set.
                flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity, Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(flowPropertyName, FlowActivityPhase.finish, null, null, null)));
                flow.addActivity(flowActivity);
                put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
            }
        }
    }
    private FlowImpl createFlow(String flowPropertyName) {
        String capitalizedFlowPropertyName = StringUtils.capitalize(flowPropertyName);
        FlowImpl flow = new FlowImpl(capitalizedFlowPropertyName+"Flow");
        flow.addPropertyDefinition(new FlowPropertyDefinitionImpl(FSSINGLE_PROPERTY_NAME).initAccess(flowLocal, internalState).initDefaultObject(flowPropertyName));
        return flow;
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
