/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.amplafi.flow.translator;

import java.util.List;

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.json.JsonRenderer;

/**
 * Translate object to/from the flow state.
 *
 * FlowTranslator implementations are best thought of as a shim interface between the flow code and the prefered serialization library.
 *
 * FlowTranslator are intended to be singletons and injected as a service.
 *
 * Sometimes it is desired to have a generic FlowTranslator that can customize at runtime for the specific class. Such FlowTranslator should extends {@link InstanceSpecificFlowTranslator}.
 *
 * FlowTranslators are registered with the {@link FlowTranslatorResolver}
 * TODO! {@link JsonRenderer} and FlowTranslators are very overlapping ... at some point reconcile!
 *
 * FlowTranslators have db transactions available.
 *
 * @param <T> the type to translate.
 */
public interface FlowTranslator <T>{
    /**
     *
     * @return The class returned by {@link FlowTranslator#deserialize(FlowPropertyProvider, FlowPropertyDefinition, DataClassDefinition, Object)}
     */
    Class<?> getTranslatedClass();
    /**
     * Do not return the actual string in many cases because this
     * FlowTranslator may not be the 'top-level' translator. (TODO:think about)
     *
     * Must be prepared to handle an object that represents an already serialized
     * version. If so the object passed in should be returned.
     * @param flowPropertyDefinition TODO
     * @param dataClassDefinition TODO
     * @param outputWriter write a version of an object.
     * @param object
     * @return outputWriter
     */
    <W> W serialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, W outputWriter, T object);

    T deserialize(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject)
            throws FlowException;

    /**
     * Determine if an instance of this class can be assigned to an
     * instance of {@link #getTranslatedClass()}. differentClass may be
     * an instance of the serialized form of {@link #getTranslatedClass()}.
     * If so then the class used store serialized form must be accepted.
     * @param differentClass
     * @return true if differentClass is a subclass of {@link #getTranslatedClass()}
     * or a subclass of the type returned by {@link #serialize(FlowPropertyDefinition, DataClassDefinition, Object, Object)
     */
    boolean isAssignableFrom(Class<?> differentClass);

    /**
     *
     * @param flowPropertyDefinition TODO
     * @param dataClassDefinition TODO
     * @param value
     * @return true if this object can be translated by the FlowTranslator to
     * the class returned by {@link FlowTranslator#deserialize(FlowPropertyProvider , FlowPropertyDefinition , DataClassDefinition, Object)}.
     */
    boolean isDeserializable(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object value);
    /**
     *
     * @param value
     * @return true if the class represents a class that this {@link FlowTranslator} could
     * return when deserializing.
     */
    boolean isDeserializedForm(Class<?> value);

    T getDefaultObject(FlowPropertyProvider flowPropertyProvider);
    /**
     * @return list of forms that this FlowTranslator can deserialize to.
     */
    List<Class<?>> getDeserializedFormClasses();

    @Deprecated // need to remove reference to specific serialization mechanism
    // only really used in BaseFlowTranslatorResolver to construct a JsonWriter
    JsonRenderer<T> getJsonRenderer();
}
