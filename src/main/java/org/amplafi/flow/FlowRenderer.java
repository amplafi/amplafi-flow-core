package org.amplafi.flow;

import java.io.Writer;

import org.amplafi.flow.web.FlowResponse;
import org.amplafi.flow.web.FlowService;

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
	 * @deprecated use {@link #render(Writer, FlowState, String, Exception)} since we're getting rid of {@link FlowService}s in future.
	 *
	 * @param flowState
	 * @param writer
	 */
	@Deprecated
	public void render(FlowResponse flowResponse);

	/**
	 * TODO
	 * @param writer TODO
	 *
	 */
	public void describeFlow(Writer writer, String flowType);

	void render(Writer writer, FlowState flowState, String errorMessage,
			Exception exception);

}
