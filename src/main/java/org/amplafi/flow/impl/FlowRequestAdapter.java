package org.amplafi.flow.impl;

import java.util.Collections;
import java.util.List;

import org.amplafi.flow.FlowState;
import org.amplafi.flow.web.FlowRequest;

/**
 * Dummy FlowREquest for shorter partial implementations.
 * 
 * @author Konstantin Burov
 *
 */
public class FlowRequestAdapter implements FlowRequest {

	@Override
	public String getParameter(String parameterName) {
		return null;
	}

	@Override
	public Iterable<String> getIterableParameter(String parameterName) {
		return null;
	}

	@Override
	public List<String> getParameterNames() {
		return null;
	}

	@Override
	public boolean hasParameter(String parameterName) {
		return false;
	}

	@Override
	public String getReferingUri() {
		return null;
	}

	@Override
	public boolean isDescribeRequest() {
		return false;
	}

	@Override
	public String getFlowType() {
		return null;
	}

	@Override
	public String getFlowId() {
		return null;
	}

	@Override
	public String getRenderResultType() {
		return null;
	}

	@Override
	public String getCompleteType() {
		return null;
	}

	@Override
	public String getAdvanceToActivity() {
		return null;
	}

	@Override
	public boolean isBackground() {
		return false;
	}

	@Override
	public Iterable<String> getPropertiesToInitialize() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasFlowState() {
		return false;
	}

	@Override
	public FlowState getFlowState() {
		return null;
	}

}
