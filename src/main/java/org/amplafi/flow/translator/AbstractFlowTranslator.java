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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.FlowException;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowTranslator;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JsonRenderer;

/**
 *
 * @author patmoore
 *
 * @param <T> class being translated.
 */
public abstract class AbstractFlowTranslator<T> implements FlowTranslator<T> {
    private FlowTranslatorResolver flowTranslatorResolver;
    protected List<Class<?>> serializedFormClasses = new ArrayList<Class<?>>();
    private List<Class<?>> deserializedFormClasses = new ArrayList<Class<?>>();
    private boolean flowTranslatorJsonRenderer;
    private JsonRenderer<T> jsonRenderer;

    @SuppressWarnings("unchecked")
    protected static final FlowTranslator<CharSequence> DEFAULT_FLOW_TRANSLATOR = CharSequenceFlowTranslator.INSTANCE;

    @SuppressWarnings("unchecked")
    protected AbstractFlowTranslator(AbstractFlowTranslator<?> flowTranslator) {
        this.flowTranslatorResolver = flowTranslator.flowTranslatorResolver;
        this.deserializedFormClasses.addAll(flowTranslator.deserializedFormClasses);
        this.serializedFormClasses.addAll(flowTranslator.serializedFormClasses);
        this.flowTranslatorJsonRenderer = flowTranslator.flowTranslatorJsonRenderer;
        this.jsonRenderer = (JsonRenderer<T>) flowTranslator.jsonRenderer;
    }
    protected AbstractFlowTranslator() {
        serializedFormClasses.add(CharSequence.class);
        this.flowTranslatorJsonRenderer = JsonRenderer.class.isAssignableFrom(this.getClass());
    }

    /**
     * @param jsonRenderer
     */
    public AbstractFlowTranslator(JsonRenderer<T> jsonRenderer) {
        this();
        this.jsonRenderer = jsonRenderer;
    }

    /**
     * @see org.amplafi.flow.FlowTranslator#deserialize(org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition , java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowException {
        if ( serializedObject == null ) {
            return null;
        } else if ( isDeserializedForm(serializedObject.getClass())) {
            return (T) serializedObject;
        } else {
            return doDeserialize(flowPropertyDefinition, dataClassDefinition, serializedObject);
        }
    }

    @Override
    public final IJsonWriter serialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, IJsonWriter jsonWriter, T object) {
        if ( jsonWriter == null ) {
            jsonWriter = getJsonWriter();
        }
        if ( object == null ) {
            return jsonWriter;
        } else if ( this.isSerializedForm(object.getClass())) {
            // already in a serialzed form? (we hope )
            return jsonWriter.value(object);
        } else {
            return doSerialize(flowPropertyDefinition, dataClassDefinition, jsonWriter, object);
        }
    }
    /**
     * @return a JsonWriter (generated by {@link org.amplafi.flow.FlowTranslatorResolver} usually )
     */
    protected IJsonWriter getJsonWriter() {
        return flowTranslatorResolver.getJsonWriter();
    }
    /**
     * @param jsonWriter
     * @param object
     * @return jsonWriter
     */
    @SuppressWarnings("unused")
    protected IJsonWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, IJsonWriter jsonWriter, T object) {
        toJson(jsonWriter, object);
        return jsonWriter;
    }

    @SuppressWarnings({"unused","unchecked"})
    protected T doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        return (T) fromJson(serializedObject);
    }

    @SuppressWarnings("unused")
    public boolean isSerializable(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object value) {
        if ( value == null ) {
            return true;
        } else {
            return isAssignableFrom(value.getClass());
        }
    }
    @Override
    public boolean isAssignableFrom(Class<?> differentClass) {
        if ( getTranslatedClass().isAssignableFrom(differentClass)) {
            return true;
        } else {
            for(Class<?> clazz: this.serializedFormClasses) {
                if (clazz.isAssignableFrom(differentClass) ) {
                    return true;
                }
            }
            for(Class<?> clazz: this.getDeserializedFormClasses()) {
                if (clazz.isAssignableFrom(differentClass) ) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean isDeserializable(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object value) {
        if ( value == null ) {
            return true;
        } else {
            return isAssignableFrom(value.getClass());
        }
    }

    @Override
    public T getDefaultObject(FlowPropertyProvider flowPropertyProvider) {
        return null;
    }
    protected void addSerializedFormClasses(Class<?>... clazz) {
        this.serializedFormClasses.addAll(Arrays.asList(clazz));
    }
    protected void addDeserializedFormClasses(Class<?>... clazz) {
        this.deserializedFormClasses.addAll(Arrays.asList(clazz));
    }
    @Override
    public boolean isDeserializedForm(Class<?> clazz) {
        return clazz == getTranslatedClass() || deserializedFormClasses.contains(clazz);
    }
    public boolean isSerializedForm(Class<?> clazz) {
        return serializedFormClasses.contains(clazz);
    }
    public void setDeserializedFormClasses(List<Class<?>> deserializedFormClasses) {
        this.deserializedFormClasses = deserializedFormClasses;
    }

    @Override
    public List<Class<?>> getDeserializedFormClasses() {
        if ( !deserializedFormClasses.contains(getTranslatedClass())) {
            deserializedFormClasses.add(getTranslatedClass());
        }
        return deserializedFormClasses;
    }

    /**
     * NOTE: May be overridden!
     * @see org.amplafi.flow.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<?> getTranslatedClass() {
        return jsonRenderer.getClassToRender();
    }

    /**
     * @param jsonWriter
     * @param object
     * @return TODO
     */
    public IJsonWriter toJson(IJsonWriter jsonWriter, T object) {
        if ( this == jsonRenderer) {
            throw new IllegalStateException(this+":infinite loop jsonRenderer==this");
        }
        return jsonRenderer.toJson(jsonWriter, object);
    }

    /**
     * @param <K>
     * @param serializedObject
     * @return the deserialized (from json) value.
     */
    @SuppressWarnings("unchecked")
    public <K> K fromJson(Object serializedObject) {
        return (K) jsonRenderer.fromJson(getTranslatedClass(), serializedObject);
    }

    /**
     * @return the jsonRenderer
     */
    @SuppressWarnings("unchecked")
    @Override
    public JsonRenderer<T> getJsonRenderer() {
        return jsonRenderer == null && this.flowTranslatorJsonRenderer? (JsonRenderer<T>)this : jsonRenderer ;
    }

    protected void setJsonRenderer(JsonRenderer<T> jsonRenderer) {
        this.jsonRenderer = jsonRenderer;
    }

    /**
     * @param flowTranslatorResolver the flowTranslatorResolver to set
     */
    public void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver) {
        this.flowTranslatorResolver = flowTranslatorResolver;
    }

    /**
     * @return the flowTranslatorResolver
     */
    public FlowTranslatorResolver getFlowTranslatorResolver() {
        return flowTranslatorResolver;
    }
}
