package org.amplafi.flow.definitions;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
import org.amplafi.flow.flowproperty.FlowPropertyExpectationImpl;
import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.flow.impl.FlowImpl;

import com.sworddance.util.NotNullIterator;

import static com.sworddance.util.CUtilities.*;

/**
 * Define flows only needed to provide a property that is used by the ui of another flow. but is not needed by the execution of the flow itself.
 * @author patmoore
 *
 */
public class FlowFromFlowPropertyDefinitionDefinitionSource implements DefinitionSource<FlowImplementor> {

    private Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();

    public FlowFromFlowPropertyDefinitionDefinitionSource() {

    }
    public FlowFromFlowPropertyDefinitionDefinitionSource(FlowPropertyDefinitionImplementor...flowPropertyDefinitionImplementors) {
        for(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor: NotNullIterator.<FlowPropertyDefinitionImplementor>newNotNullIterator(flowPropertyDefinitionImplementors)) {
            String flowPropertyName = flowPropertyDefinitionImplementor.getName();
            FlowImpl flow = new FlowImpl(flowPropertyName+"Flow");
            FlowActivityImpl flowActivity = new FlowActivityImpl(flowPropertyName+"FlowActivity");
            flowActivity.addPropertyDefinition(flowPropertyDefinitionImplementor);
            flow.addActivity(flowActivity);
            put(flows, flow.getFlowPropertyProviderFullName(), flow);
        }
    }

    // HACK having to pass in the property name seems weak.
    public void add(String flowPropertyName, FlowPropertyDefinitionProvider flowPropertyDefinitionProvider) {
        FlowImpl flow = new FlowImpl(flowPropertyName+"Flow");
        FlowActivityImpl flowActivity = new FlowActivityImpl("FA1");
        flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity);
        flow.addActivity(flowActivity);
        put(flows, flow.getFlowPropertyProviderFullName(), flow);
    }
    // HACK: assuming that all properties should be visible as a flow also feels bad.S
    public void add(FlowPropertyDefinitionProvider... flowPropertyDefinitionProviders) {
        for(FlowPropertyDefinitionProvider flowPropertyDefinitionProvider :flowPropertyDefinitionProviders) {
            for(String flowPropertyName : flowPropertyDefinitionProvider.getFlowPropertyDefinitionNames()) {
                FlowImpl flow = new FlowImpl(flowPropertyName+"Flow");
                FlowActivityImpl flowActivity = new FlowActivityImpl("FA1");
                flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity, Arrays.<FlowPropertyExpectation>asList(new FlowPropertyExpectationImpl(FlowActivityPhase.finish)));
                flow.addActivity(flowActivity);
                put(flows, flow.getFlowPropertyProviderFullName(), flow);
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
