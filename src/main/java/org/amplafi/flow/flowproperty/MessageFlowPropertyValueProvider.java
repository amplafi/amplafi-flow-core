/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowUtils;
import org.amplafi.flow.PropertyUsage;

/**
 * used to return standard message keys.
 * @author patmoore
 *
 */
public class MessageFlowPropertyValueProvider implements FlowPropertyValueProvider<FlowActivity> {
    private String standardPrefix;

    public static final MessageFlowPropertyValueProvider INSTANCE = new MessageFlowPropertyValueProvider("message:");
    /**
     * @param standardPrefix
     */
    public MessageFlowPropertyValueProvider(String standardPrefix) {
        this.standardPrefix =standardPrefix;
    }
    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        StringBuilder standardKey = new StringBuilder(standardPrefix);
        Flow flow = flowActivity.getFlow();
        standardKey.append("flow.").append(FlowUtils.INSTANCE.toLowerCase(flow.getFlowTypeName())).append(".");
        if (flowPropertyDefinition.getPropertyUsage() == PropertyUsage.activityLocal) {
            standardKey.append(FlowUtils.INSTANCE.toLowerCase(flowActivity.getActivityName())).append(".");
        }
        if (flowPropertyDefinition.getName().startsWith("fs") || flowPropertyDefinition.getName().startsWith("fa")) {
            standardKey.append(FlowUtils.INSTANCE.toLowerCase(flowPropertyDefinition.getName().substring(2)));
        } else {
            standardKey.append(FlowUtils.INSTANCE.toLowerCase(flowPropertyDefinition.getName()));
        }
        return (T) standardKey.toString();
    }

}
