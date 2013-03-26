package org.amplafi.flow.definitions;

/**
 * Implementors represent a FlowDefinition.
 * @author patmoore
 *
 */
public interface FlowDefinition {
    /**
     * @return get the flow name as it should appear in the flowentry and the
     *         titlebar.
     */
    String getFlowTitle();

    /**
     * @return Used if this is a secondary flow that will be started as the next
     *         flow.
     */
    String getContinueFlowTitle();

    /**
     * @return returns the link title text.
     */
    String getLinkTitle();

    /**
     *
     * @return display this text on a mouseover hover on the entry point.
     */
    String getMouseoverEntryPointText();

    /**
     * @return Explanatory text about what the purpose of this flow is.
     */
    String getFlowDescriptionText();
}
