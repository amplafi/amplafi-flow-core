package org.amplafi.flow.translator;

public interface FlowRenderer<T> {
    <W extends SerializationWriter> W toSerialization(W serializationWriter, T o);
    /**
     *
     * @param <K>
     * @param clazz the class of the object that is the expected output ( not necessarily <K> as the result may be something else (for example an id))
     * @param value serialization representation of some sort, JSONObject, JSONArray, string, etc.
     * @param parameters optional additional parameters
     * @return the object translated from the serialization format object.
     */
    public <K> K fromSerialization(Class<K> clazz, Object value, Object... parameters);
    public Class<? extends T> getClassToRender();
}
