package org.amplafi.flow;

import java.io.Writer;

import org.amplafi.flow.web.FlowResponse;

public class BaseFlowResponse implements FlowResponse {

	private final Writer writer;
	private Exception exception;
	private String errorMessage;
	private FlowState flowState;
	private String redirect;

	public BaseFlowResponse(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void setError(String message, Exception exception) {
		errorMessage = message;
		this.exception = exception;
	}

	@Override
	public void render(FlowRenderer renderer) {
		renderer.render(this);
	}

	@Override
	public boolean hasErrors() {
		return exception != null || errorMessage != null;
	}

	@Override
	public Writer getWriter() {
		return writer;
	}

	@Override
	public FlowState getFlowState() {
		return flowState;
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void setFlowState(FlowState flowState) {
		this.flowState = flowState;
	}

	@Override
	public String getRedirect() {
		String redirect = null;
		if (flowState != null && flowState.getCurrentPage() != null && flowState.getCurrentPage().startsWith("http")) {
			redirect = flowState.getCurrentPage();
		} 
		return redirect;
	}

	@Override
	public boolean isRedirectSet() {
		return getRedirect() != null;
	}
}