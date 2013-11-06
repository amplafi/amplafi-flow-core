package org.amplafi.flow;

/**
 * Exception thrown if there is an error defining flows, flow activities or flow properties
 * @author patmoore
 *
 */
public class FlowConfigurationException extends FlowException {

    private static final long serialVersionUID = 1L;
    /**
     * @param exception
     */
    public FlowConfigurationException(Exception exception) {
        super(exception);
    }

    /**
     * @param flowState
     * @param messages
     */
    public FlowConfigurationException(FlowState flowState, Object... messages) {
        super(flowState, messages);
    }

    /**
     * @param flowState
     */
    public FlowConfigurationException(FlowState flowState) {
        super(flowState);
    }

    /**
     * @param messages
     */
    public FlowConfigurationException(Object... messages) {
        super(messages);
    }

    /**
     * @param message
     * @param exception
     */
    public FlowConfigurationException(String message, Exception exception) {
        super(message, exception);
    }
    /**
     * @param failMessageParts any number of objects that are concatenated and converted to strings only if message is thrown.
     * @param validResult if true then return null. Otherwise throw {@link FlowConfigurationException}.
     * @return null always
     * @throws FlowConfigurationException if validResult is false.
     */
    public static FlowConfigurationException valid(boolean validResult, Object... failMessageParts) {
        if (!validResult) {
            throw new FlowConfigurationException(failMessageParts);
        }
        return null;
    }
}
