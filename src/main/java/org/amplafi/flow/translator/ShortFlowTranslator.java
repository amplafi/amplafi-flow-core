package org.amplafi.flow.translator;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.InconsistencyTracking;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.renderers.NumberJsonRenderer;



public class ShortFlowTranslator extends AbstractFlowTranslator<Short> {

    public ShortFlowTranslator() {
        super(NumberJsonRenderer.INSTANCE);
        this.addSerializedFormClasses(Number.class, int.class, long.class, short.class);
        this.addDeserializedFormClasses(short.class);
    }
    @SuppressWarnings("unchecked")
    @Override
    public Short deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        if (serializedObject == null ){
            return null;
        } else if ( serializedObject instanceof Number) {
            return new Short(((Number)serializedObject).shortValue());
        }
        String s = serializedObject.toString();
        try {
            return new Short(s);
        } catch(NumberFormatException e) {
            throw new FlowValidationException(new InconsistencyTracking("cannot-be-parsed",
                    s+": contains non-numerics"));
        }
    }

    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, Short object) {
        jsonWriter.value(object);
        return jsonWriter;
    }

    @Override
    public Class<Short> getTranslatedClass() {
        return Short.class;
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
    public Short getDefaultObject(FlowActivity flowActivity) {
        return 0;
    }
    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.flowproperty.FlowPropertyDefinition, org.amplafi.flow.flowproperty.DataClassDefinition, java.lang.Object)
     */
    @Override
    protected Short doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        throw new UnsupportedOperationException();
    }
}
