package org.amplafi.flow.translator;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.InconsistencyTracking;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;
import org.amplafi.json.renderers.NumberJsonRenderer;



public class IntegerFlowTranslator extends AbstractFlowTranslator<Integer> {

    public IntegerFlowTranslator() {
        super((JsonRenderer<Integer>)NumberJsonRenderer.INSTANCE);
        this.addSerializedFormClasses(Number.class, int.class, long.class, short.class, Integer.class);
        this.addDeserializedFormClasses(int.class);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Integer doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        if ( serializedObject instanceof Number) {
            return new Integer(((Number)serializedObject).intValue());
        }
        String s = serializedObject.toString();
        try {
            return new Integer(s);
        } catch(NumberFormatException e) {
            throw new FlowValidationException(new InconsistencyTracking("cannot-be-parsed",
                    s+": contains non-numerics"));
        }
    }

    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, Integer object) {
        return jsonWriter.value(object);
    }

    @Override
    public Class<Integer> getTranslatedClass() {
        return Integer.class;
    }

    @Override
    public boolean isDeserializable(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object value) {
        if ( super.isDeserializable(flowPropertyDefinition, dataClassDefinition, value)) {
            return true;
        } else {
            return Number.class.isAssignableFrom(value.getClass());
        }
    }
    @Override
    public Integer getDefaultObject(FlowActivity flowActivity) {
        return 0;
    }
}
