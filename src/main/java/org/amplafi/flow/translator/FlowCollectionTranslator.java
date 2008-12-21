package org.amplafi.flow.translator;

import java.util.Collection;

import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.translator.AbstractFlowTranslator;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.JSONArray;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;
import org.apache.commons.lang.ObjectUtils;



public abstract class FlowCollectionTranslator<C extends Iterable<? extends T>, T> extends AbstractFlowTranslator<C> {
    public FlowCollectionTranslator() {
        this.addSerializedFormClasses(JSONArray.class);
    }
    /**
     * @param jsonRenderer
     */
    public FlowCollectionTranslator(JsonRenderer<C> jsonRenderer) {
        super(jsonRenderer);
        this.addSerializedFormClasses(JSONArray.class);
    }
    @SuppressWarnings("unchecked")
    protected void deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Collection<T> collection, Object serialized) {
        JSONArray jsonArray;
        if ( serialized instanceof JSONArray) {
            jsonArray = (JSONArray) serialized;
        } else {
            jsonArray = new JSONArray(ObjectUtils.toString(serialized, null));
        }
        for(Object o : jsonArray.asList()) {
            T element= (T) dataClassDefinition.getElementDataClassDefinition().deserialize(flowPropertyDefinition, o);
            collection.add(element);
        }
    }
    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, C object) {
        jsonWriter.array();
        for(T element: object) {
            dataClassDefinition.getElementDataClassDefinition().serialize(flowPropertyDefinition, jsonWriter, element);
        }
        return jsonWriter.endArray();
    }
    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.flowproperty.FlowPropertyDefinition, org.amplafi.flow.flowproperty.DataClassDefinition, java.lang.Object)
     */
    @Override
    protected C doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        throw new UnsupportedOperationException();
    }

}
