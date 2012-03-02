package org.amplafi.flow.web;

import java.io.Writer;

import org.amplafi.flow.FlowRenderer;
import org.amplafi.flow.FlowState;

/**
 * Implementors store results of and provide resources to fulfill FlowRequests.
 * 
 * @author aectann@gmail.com
 *
 */
public interface FlowResponse {

	FlowState getFlowState();
	
	Exception getException();
	
	boolean hasErrors();

	Writer getWriter();

	String getErrorMessage();

	void render(FlowRenderer renderer);

	void setError(String message, Exception exception);

	void setFlowState(FlowState flowState);
	
	String getRedirect();
	
	boolean isRedirectSet();
}