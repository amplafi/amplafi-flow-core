package org.amplafi.flow.impl;

import java.io.Writer;

import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowRenderer;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.web.FlowRequest;

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
	public void render(FlowState flowState, Writer writer) {
	}

	@Override
	public void renderError(FlowState flowState, String message,
			Exception exception, Writer writer) {
	}

	@Override
	public void describe(FlowRequest flowRequest) {
	}

}
