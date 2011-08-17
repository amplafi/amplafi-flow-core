package org.amplafi.flow.web;

import java.io.PrintWriter;
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
	
	String getReferingUri();
	
	PrintWriter getWriter();
}
