package org.amplafi.flow.web;

public class FlowNotFoundException extends Exception {

	private static final long serialVersionUID = 165522773139906305L;

	public FlowNotFoundException(String flowName) {
		super(flowName);
	}
	
}
