package org.amplafi.flow.web;

import java.io.PrintWriter;
import java.util.List;

public interface FlowRequest {
	
	String getParameter(String parameterName);
	
	List<String> getParameterNames();
	
	String getReferingUri();
	
	PrintWriter getWriter();
}
