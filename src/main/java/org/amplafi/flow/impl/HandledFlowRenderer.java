package org.amplafi.flow.impl;

import java.io.Writer;

import org.amplafi.flow.Flow;
import org.amplafi.flow.FlowConstants;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowStateRenderer;
import org.amplafi.flow.FlowState;

/**
 * A renderer which does nothing, for everything is handled by some external code.
 *
 * @author Konstantin Burov
 *
 */
public class HandledFlowRenderer implements FlowStateRenderer {


	@Override
	public String getRenderResultType() {
		return FlowConstants.HANDLED;
	}


	@Override
	public void render(Writer writer, FlowState flowState, String errorMessage,
			Exception exception) {
	}

    @Override
    public void describeFlow(Writer writer, Flow flowType) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void describeApi(Writer writer, FlowManagement flowManagement) {
        // TODO Auto-generated method stub
        
    }

}
