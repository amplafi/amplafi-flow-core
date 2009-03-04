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

import java.util.TimeZone;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;


/**
 * @author patmoore
 *
 */
public class TimezoneFlowTranslator extends AbstractFlowTranslator<TimeZone> implements JsonRenderer<TimeZone> {

    /**
     * @see org.amplafi.flow.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<TimeZone> getTranslatedClass() {
        return TimeZone.class;
    }

    /**
     * @see org.amplafi.flow.FlowTranslator#serialize(org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition , org.amplafi.json.JSONWriter, java.lang.Object)
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
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition , java.lang.Object)
     */
    @Override
    protected TimeZone doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        return (TimeZone) fromJson(dataClassDefinition.getDataClass(), serializedObject);
    }

}
