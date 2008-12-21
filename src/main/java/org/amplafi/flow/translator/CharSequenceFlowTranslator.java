package org.amplafi.flow.translator;

import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.JSONObject;
import org.amplafi.json.JSONStringer;
import org.amplafi.json.JSONWriter;
import org.apache.commons.lang.ObjectUtils;


/**
 * serialize CharSequence objects.
 * Copyright 2008 by Amplafi. All right reserved.
 *
 */
public class CharSequenceFlowTranslator<T> extends AbstractFlowTranslator<T> {

    public static final CharSequenceFlowTranslator INSTANCE = new CharSequenceFlowTranslator();
    public CharSequenceFlowTranslator() {
        // potentially anything could be deserialized from a String.
        // this allows any Object to be serialized by this FlowTranslator.
        addDeserializedFormClasses(Object.class);
    }
    @Override
    public T deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        return (T) JSONObject.unquote(ObjectUtils.toString(serializedObject, null));
    }

    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, T object) {
        return jsonWriter.value(object);
    }


    @Override
    public Class<CharSequence> getTranslatedClass() {
        return CharSequence.class;
    }

    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.flowproperty.FlowPropertyDefinition, org.amplafi.flow.flowproperty.DataClassDefinition, java.lang.Object)
     */
    @Override
    protected T doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition,
        Object serializedObject) throws FlowValidationException {
        return (T) JSONObject.unquote(serializedObject.toString());
    }
    @Override
    public JSONWriter getJsonWriter() {
        return new JSONStringer();
    }

}
