package org.amplafi.flow.impl;

import java.io.Writer;

import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowRenderer;
import org.amplafi.flow.FlowState;

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
	public void describeFlow(Writer writer, String flowType) {

	}

	@Override
	public void render(Writer writer, FlowState flowState, String errorMessage,
			Exception exception) {
	}

}
