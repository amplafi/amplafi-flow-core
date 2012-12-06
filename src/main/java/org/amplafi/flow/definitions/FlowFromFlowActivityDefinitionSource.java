package org.amplafi.flow.definitions;

import static com.sworddance.util.CUtilities.put;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.impl.FlowImpl;
import org.apache.commons.lang.StringUtils;

public class FlowFromFlowActivityDefinitionSource implements DefinitionSource<FlowImplementor> {
    public static final String FLOW_PREFIX = "Flow";

    private final Map<String, FlowImplementor> flows = new ConcurrentHashMap<String, FlowImplementor>();

    public void add(FlowActivityImplementor flowActivityImplementor) {
        String capitalizedFlowActivityName = StringUtils.capitalize(flowActivityImplementor.getFlowPropertyProviderFullName());
        FlowImpl flow = new FlowImpl(capitalizedFlowActivityName+FLOW_PREFIX);
        flow.addActivity(flowActivityImplementor);
        put(this.flows, flow.getFlowPropertyProviderFullName(), flow);
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
