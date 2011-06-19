package org.amplafi.flow.web;

import org.amplafi.flow.FlowState;

public class FlowRedirectException extends Exception {

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

	public FlowState getFlowState() {
		return flowState;
	}
}
