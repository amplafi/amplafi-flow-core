package org.amplafi.flow;

import java.util.List;

import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;

/**
 *
 * Copyright 2008 by Amplafi. All right reserved.
 *
 */

/**
 * Translate object to/from the flow state.
 * TODO! {@link JsonRenderer} and FlowTranslators are very overlapping ... at some point reconcile!
 * @param <T> the type to translate.
 */
public interface FlowTranslator <T>{
    /**
     *
     * @return The class returned by {@link FlowTranslator#deserialize(FlowPropertyDefinition , DataClassDefinition , Object)}.
     */
    public Class<?> getTranslatedClass();
    /**
     * Do not return the actual string in many cases because this
     * FlowTranslator may not be the 'top-level' translator. (TODO:think about)
     *
     * Must be prepared to handle an object that represents an already serialized
     * version. If so the object passed in should be returned.
     * @param flowPropertyDefinition TODO
     * @param dataClassDefinition TODO
     * @param jsonWriter write a version of an object.
     * @param object
     */
    public JSONWriter serialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, T object);

    public T deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject)
            throws FlowException;

    /**
     * Determine if an instance of this class can be assigned to an
     * instance of {@link #getTranslatedClass()}. differentClass may be
     * an instance of the serialized form of {@link #getTranslatedClass()}.
     * If so then the class used store serialized form must be accepted.
     * @param differentClass
     * @return true if differentClass is a subclass of {@link #getTranslatedClass()}
     * or a subclass of the type returned by {@link #serialize(FlowPropertyDefinition , DataClassDefinition , JSONWriter, Object)}
     */
    public boolean isAssignableFrom(Class<?> differentClass);

    /**
     *
     * @param flowPropertyDefinition TODO
     * @param dataClassDefinition TODO
     * @param value
     * @return true if this object can be translated by the FlowTranslator to
     * the class returned by {@link FlowTranslator#deserialize(FlowPropertyDefinition , DataClassDefinition , Object)}.
     */
    public boolean isDeserializable(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object value);
    /**
     *
     * @param value
     * @return true if the class represents a class that this {@link FlowTranslator} could
     * return when deserializing.
     */
    public boolean isDeserializedForm(Class<?> value);

    public T getDefaultObject(FlowActivity flowActivity);
    /**
     * @return list of forms that this FlowTranslator can deserialize to.
     */
    public List<Class<?>> getDeserializedFormClasses();

    public JsonRenderer<T> getJsonRenderer();
}
