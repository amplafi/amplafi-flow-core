package org.amplafi.flow.definitions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowExecutionException;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.impl.FlowImpl;
import org.apache.commons.lang.StringUtils;

/**
 *
 * A FlowFromFlowActivityDefinitionSource is a {@link DefinitionSource} that packages {@link FlowActivity}
 * into {@link FlowImplementor}. Used to avoid the boilerplate of flows when all you have are single flow activities, for example in
 * stateless server-client exchanges
 *
 *
 */
public class FlowFromFlowActivityDefinitionSource implements DefinitionSource<FlowImplementor> {

    private final Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();

    public FlowFromFlowActivityDefinitionSource add(FlowActivityImplementor flowActivityImplementor) {
        String capitalizedFlowActivityName = StringUtils.capitalize(flowActivityImplementor.getFlowPropertyProviderFullName());
        if ( capitalizedFlowActivityName.endsWith("FlowActivity")) {
            capitalizedFlowActivityName = capitalizedFlowActivityName.substring(0, capitalizedFlowActivityName.lastIndexOf("FlowActivity"));
        }
        return this.add(capitalizedFlowActivityName, flowActivityImplementor);
    }
    public FlowFromFlowActivityDefinitionSource add(String capitalizedFlowActivityName, FlowActivityImplementor flowActivityImplementor) {
        FlowImpl flow = new FlowImpl(capitalizedFlowActivityName);
        flow.addActivity(flowActivityImplementor);
        if ( this.flows.containsKey(flow.getFlowPropertyProviderFullName())) {
            // early check to help spot issues.
            throw new FlowExecutionException(flow.getFlowPropertyProviderFullName() + " defined twice");
        }
        this.flows.put(flow.getFlowPropertyProviderFullName(), flow);
        return this;
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
