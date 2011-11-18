/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
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

	boolean isBackground();

	Iterable<String> getPropertiesToInitialize();

	/**
	 *
	 * @param status - one of HttpStatus codes
	 */
	void setStatus(int status);
}
