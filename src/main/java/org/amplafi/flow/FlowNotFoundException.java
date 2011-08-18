package org.amplafi.flow;


public class FlowNotFoundException extends FlowException {

	private static final long serialVersionUID = 165522773139906305L;

	public FlowNotFoundException(String flowName) {
		super(flowName);
	}

}
