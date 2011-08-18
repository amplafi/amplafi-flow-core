package org.amplafi.flow.web;

import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowState;

/**
 * TODO KOSTYA - Why not pass just the FlowState? Why is an exception needed? Based on the usage - I
 * don't see the need.
 */
public class FlowRedirectException extends FlowException {

    private static final long serialVersionUID = -5566708491987857350L;

    private final String page;

    private final FlowState flowState;

    public FlowRedirectException(String page, FlowState flowState) {
        this.page = page;
        this.flowState = flowState;
    }

    public String getPage() {
        return page;
    }

    @Override
    public FlowState getFlowState() {
        return flowState;
    }
}
