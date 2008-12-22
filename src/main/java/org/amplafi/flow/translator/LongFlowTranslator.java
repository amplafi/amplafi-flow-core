package org.amplafi.flow.translator;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.InconsistencyTracking;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.renderers.NumberJsonRenderer;



public class LongFlowTranslator extends AbstractFlowTranslator<Long> {

    public LongFlowTranslator() {
        super(NumberJsonRenderer.INSTANCE);
        this.addSerializedFormClasses(Number.class, int.class, long.class, short.class);
        this.addDeserializedFormClasses(long.class);
    }
    @SuppressWarnings("unchecked")
    @Override
    public Long deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        if (serializedObject == null ){
            return null;
        } else if ( serializedObject instanceof Number) {
            return new Long(((Number)serializedObject).longValue());
        }
        String s = serializedObject.toString();
        try {
            return new Long(s);
        } catch(NumberFormatException e) {
            throw new FlowValidationException(new InconsistencyTracking("cannot-be-parsed",
                    s+": contains non-numerics"));
        }
    }

    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, Long object) {
        return jsonWriter.value(object);
    }

    @Override
    public Class<Long> getTranslatedClass() {
        return Long.class;
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
    public Long getDefaultObject(FlowActivity flowActivity) {
        return 0L;
    }
    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition , java.lang.Object)
     */
    @Override
    protected Long doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        throw new UnsupportedOperationException();
    }
}
