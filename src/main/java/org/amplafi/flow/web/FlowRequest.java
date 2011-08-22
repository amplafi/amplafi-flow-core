package org.amplafi.flow.web;

import java.io.Writer;
import java.util.List;

public interface FlowRequest {
	
	String getParameter(String parameterName);
	
	/**
	 * For properties that represent JSONArrays or comma separated values.
	 * It's up to implementation to decide what it expects.
	 * 
	 * @param parameterName
	 * @return
	 */
	Iterable<String> getIterableParameter(String parameterName);
	
	List<String> getParameterNames();
	
	boolean hasParameter(String parameterName);
	
	String getReferingUri();
	
	Writer getWriter();
	
	boolean isDescribeRequest();
	
	String getFlowType();
	
	String getFlowId();

	String getRenderResultType();
	
	String getCompleteType();
	
	String getAdvanceToActivity();
	
	boolean isBackgorund();
	
	Iterable<String> getPropertiesToInitialize();
	
	/**
	 *  
	 * @param status - one of HttpStatus codes
	 */
	void setStatus(int status);
}
