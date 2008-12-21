/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.translator;

import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonSelfRenderer;


/**
 * @author patmoore
 *
 */
public class JsonSelfRendererFlowTranslator extends AbstractFlowTranslator {

    /**
     * @see org.amplafi.flow.translator.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<JsonSelfRenderer> getTranslatedClass() {
        return JsonSelfRenderer.class;
    }

    /**
     * @see org.amplafi.flow.translator.FlowTranslator#serialize(org.amplafi.flow.flowproperty.FlowPropertyDefinition, org.amplafi.flow.flowproperty.DataClassDefinition, org.amplafi.json.JSONWriter, java.lang.Object)
     */
    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter,
        Object object) {
        return jsonWriter.value(object);
    }

    @Override
    protected Object doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        try {
            JsonSelfRenderer jsonSelfRenderer = (JsonSelfRenderer) dataClassDefinition.getDataClass().newInstance();
            return jsonSelfRenderer.fromJson(serializedObject);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
