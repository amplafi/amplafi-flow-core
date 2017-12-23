package org.amplafi.flow;

import org.amplafi.flow.translator.SerializationWriter;

public interface SerializationProvider {

    /**
     *
     */
    <W extends SerializationWriter> W getSerializationWriter();
    /**
     * something to handle deserialization
     */
}
