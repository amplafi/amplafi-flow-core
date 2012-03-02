package org.amplafi.flow;

import org.amplafi.flow.web.FlowRequest;
import org.amplafi.flow.web.FlowResponse;

/**
 * Implementations render the flow output for at the conclusion of processing a flow request.
 * @author patmoore
 *
 */
public interface FlowRenderer {

	/**
	 * @return json, html, xml etc..
	 */
	public String getRenderResultType();

	/**
	 * TODO
	 *
	 * @param flowState
	 * @param writer
	 */
	public void render(FlowResponse flowResponse);

	/**
	 * TODO
	 *
	 */
	public void describeFlow(FlowResponse flowRequest, String flowType);

}
