package org.amplafi.flow.definitions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider;
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

    public void add(String flowTypeName, FlowPropertyDefinitionProvider flowPropertyDefinitionProvider) {
        FlowImpl flow = new FlowImpl(flowTypeName);
        FlowActivityImpl flowActivity = new FlowActivityImpl(flowTypeName+"Activity");
        flowPropertyDefinitionProvider.defineFlowPropertyDefinitions(flowActivity);
        flow.addActivity(flowActivity);
        put(flows, flow.getFlowPropertyProviderFullName(), flow);
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
