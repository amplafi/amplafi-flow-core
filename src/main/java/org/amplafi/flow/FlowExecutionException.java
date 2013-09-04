package org.amplafi.flow;

/**
 * The flow experience an exception while executed.
 *
 * Used to wrap up runtime exceptions
 * @author patmoore
 *
 */
public class FlowExecutionException extends FlowException {
    public FlowExecutionException(String message, Exception exception) {
        super(message, exception);
    }
    public FlowExecutionException(String message) {
        super(message);
    }
}
