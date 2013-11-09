package org.amplafi.flow.definitions;

import java.util.List;
import java.util.Map;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivityImplementor;
import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowImplementor;
import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.impl.FlowImpl;
import org.apache.commons.lang.StringUtils;

public class FlowDefinitionBuilder {

    private String name;
    private List<FlowActivityImplementor> flowActivities;
    private Map<String, FlowPropertyDefinitionBuilder> flowPropertyDefinitions;
    public FlowDefinitionBuilder(String name) {
        this.name = name;
    }

    public FlowDefinitionBuilder(FlowImplementor flow) {

    }

    public FlowDefinitionBuilder addActivity(FlowActivityImplementor flowActivity) {
        for(FlowActivityImplementor flowActivityImplementor: flowActivities) {
            if (flowActivityImplementor.isFlowPropertyProviderNameSet() && flowActivity.isFlowPropertyProviderNameSet() && StringUtils.equalsIgnoreCase(flowActivity.getFlowPropertyProviderName(), flowActivityImplementor.getFlowPropertyProviderName())) {
                throw new FlowConfigurationException(flowActivityImplementor.getFlowPropertyProviderName()+": A FlowActivity with the same name has already been added to this flow. existing="+flowActivityImplementor+" new="+flowActivity);
            }
        }
        this.flowActivities.add(flowActivity);

        return this;
    }

    public <T extends Flow> T toFlow() {
        FlowImpl flow = new FlowImpl(this.name);

        for(FlowActivityImplementor flowActivityImplementor: flowActivities) {
            flowActivityImplementor.setFlow(flow);
            flowActivityImplementor.processDefinitions();
        }
        flow.setActivities(this.flowActivities);
        return (T) flow;
    }
}
