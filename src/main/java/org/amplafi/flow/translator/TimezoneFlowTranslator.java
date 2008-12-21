/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.translator;

import java.util.TimeZone;

import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;


/**
 * @author patmoore
 *
 */
public class TimezoneFlowTranslator extends AbstractFlowTranslator<TimeZone> implements JsonRenderer<TimeZone> {

    /**
     * @see org.amplafi.flow.translator.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<TimeZone> getTranslatedClass() {
        return TimeZone.class;
    }

    /**
     * @see org.amplafi.flow.translator.FlowTranslator#serialize(org.amplafi.flow.flowproperty.FlowPropertyDefinition, org.amplafi.flow.flowproperty.DataClassDefinition, org.amplafi.json.JSONWriter, java.lang.Object)
     */
    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, TimeZone object) {
        toJson(jsonWriter, object);
        return jsonWriter;
    }

    /**
     * @param jsonWriter
     * @param object
     * @return jsonWriter
     */
    @Override
    public JSONWriter toJson(JSONWriter jsonWriter, TimeZone object) {
        return jsonWriter.value(object.getID());
    }

    /**
     * @see org.amplafi.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, java.lang.Object[])
     */
    @Override
    public <K> K fromJson(Class<K> clazz, Object value, Object... parameters) {
        return (K) TimeZone.getTimeZone(value.toString());
    }

    /**
     * @see org.amplafi.json.JsonRenderer#getClassToRender()
     */
    @Override
    public Class<? extends TimeZone> getClassToRender() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.flowproperty.FlowPropertyDefinition, org.amplafi.flow.flowproperty.DataClassDefinition, java.lang.Object)
     */
    @Override
    protected TimeZone doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        return (TimeZone) fromJson(dataClassDefinition.getDataClass(), serializedObject);
    }

}
