package org.amplafi.flow;

import java.io.Writer;

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
	 * @param writer TODO
	 *
	 */
	public void describeFlow(Writer writer, Flow flowType);

	void render(Writer writer, FlowState flowState, String errorMessage,
			Exception exception);

    void describeApi(Writer writer, FlowManagement flowManagement);

}
