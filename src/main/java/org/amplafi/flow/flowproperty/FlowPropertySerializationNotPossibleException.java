package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowState;

/**
 * Should be thrown when a flow property serialization is impossible for any reason.
 *
 * @author aectann@gmail.com (Konstantin Burov)
 *
 */
public class FlowPropertySerializationNotPossibleException extends FlowException {

    private static final long serialVersionUID = 665430229497830087L;

    public FlowPropertySerializationNotPossibleException(FlowState flowState, String message) {
        super(flowState, message);
    }

    public FlowPropertySerializationNotPossibleException(FlowState flowState) {
        super(flowState);
    }

}
