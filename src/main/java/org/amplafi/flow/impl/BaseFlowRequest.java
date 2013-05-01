package org.amplafi.flow.impl;

import static org.amplafi.flow.FlowConstants.DESCRIBE;
import static org.amplafi.flow.FlowConstants.FSRENDER_RESULT;
import static org.amplafi.flow.launcher.FlowLauncher.ADV_FLOW_ACTIVITY;
import static org.amplafi.flow.launcher.FlowLauncher.COMPLETE_FLOW;
import static org.amplafi.flow.launcher.FlowLauncher.FLOW_ID;

import org.amplafi.flow.ServicesConstants;
import org.amplafi.flow.web.FlowRequest;
import static org.apache.commons.lang.StringUtils.*;
/**
 * @author Konstantin Burov
 *
 */
public abstract class BaseFlowRequest implements FlowRequest{

    // see amplafi.flow.js
    private static final String FS_BACKGROUND_FLOW = "fsInBackground";
    private String renderResultDefault;

    protected BaseFlowRequest(String renderResultDefault) {
        this.renderResultDefault = renderResultDefault;
    }
    @Override
    public boolean hasParameter(String parameterName) {
        return getParameterNames().contains(parameterName);
    }

    @Override
    public boolean isDescribeRequest() {
        return hasParameter(DESCRIBE);
    }

    @Override
    public String getAdvanceToActivity() {
        return getParameter(ADV_FLOW_ACTIVITY);
    }

    @Override
    public String getCompleteType() {
        return getParameter(COMPLETE_FLOW);
    }

    @Override
    public String getRenderResultType() {
        String renderResultType = getParameter(FSRENDER_RESULT);
        if ( isNotBlank(renderResultType)) {
            return renderResultType;
        } else {
            return renderResultDefault;
        }
    }

    @Override
    public String getFlowId() {
        return getParameter(FLOW_ID);
    }

    @Override
    public String getFlowType() {
        return getParameter(ServicesConstants.FLOW_TYPE);
    }

    @Override
    public boolean isBackground() {
        return hasParameter(FS_BACKGROUND_FLOW);
    }

}
