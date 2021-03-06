package org.amplafi.flow;

/**
 * The flow experienced an exception caused by programming bug while executed.
 *
 * Used to wrap up runtime exceptions
 * @author patmoore
 *
 */
public class FlowExecutionException extends FlowException {
    private static final long serialVersionUID = 1L;
    public FlowExecutionException(String message, Exception exception) {
        super(message, exception);
    }
    public FlowExecutionException(String message) {
        super(message);
    }
    public FlowExecutionException(Exception exception) {
        super(exception);
    }
    /**
     * @param messages
     */
    public FlowExecutionException(Object... messages) {
        super(messages);
    }
}
