package org.amplafi.flow.impl;

import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowRenderer;
import org.amplafi.flow.web.FlowResponse;

/**
 * A renderer which does nothing, for everything is handled by some external code. 
 * 
 * @author Konstantin Burov
 *
 */
public class HandledFlowRenderer implements FlowRenderer {


	@Override
	public String getRenderResultType() {
		return FlowConstants.HANDLED;
	}

	@Override
	public void render(FlowResponse flowResponse) {
	}

	@Override
	public void describeFlow(FlowResponse flowRequest, String flowType) {
		
	}

}
