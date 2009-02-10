package org.amplafi.flow.translator;

import org.amplafi.json.JSONObject;
import org.amplafi.json.JSONWriter;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.validation.FlowValidationException;

public class JSONObjectFlowTranslator extends AbstractFlowTranslator<JSONObject> {
    @Override
    public Class<?> getTranslatedClass() {
        return JSONObject.class;
    }

    @Override
    protected JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition,
                                     JSONWriter jsonWriter, JSONObject object) {
        // empty json are appended as null but here we want to preserve them, so write them as {}
        if (object!=null && object.equals(null)) {
            jsonWriter.object();
            jsonWriter.endObject();
            return jsonWriter;
        }
        return jsonWriter.value(object);
    }

    @Override
    protected JSONObject doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition,
                                       Object serializedObject) throws FlowValidationException {
            return JSONObject.toJsonObject(serializedObject);
    }
}
