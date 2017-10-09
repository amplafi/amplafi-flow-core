package org.amplafi.flow;

import org.amplafi.flow.translator.SerializationWriter;

public interface FlowSelfRenderer<E> extends FlowRenderer<E> {
    public <W extends SerializationWriter> W toSerialization(W writer);
    /**
     * @param <T> class returned.
     * @param object some JSON object. It should be the same kind of object written by the {@link #toJson(IJsonWriter)} method.
     * @return the created object. Usually this (but it doesn't have to be)
     */
    public <T> T fromSerialization(Object object);
}
