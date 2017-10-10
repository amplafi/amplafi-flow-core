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
import org.amplafi.flow.FlowRenderer;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.validation.FlowValidationException;


/**
 *
 * Translate a TimeZone ID to and from a {@link TimeZone} object
 * @author patmoore
 *
 */
public class TimezoneFlowTranslator extends AbstractFlowTranslator<TimeZone> implements FlowRenderer<TimeZone> {

    /**
     * @see org.amplafi.flow.translator.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<TimeZone> getTranslatedClass() {
        return TimeZone.class;
    }

    @Override
    protected <W extends SerializationWriter> W doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, W jsonWriter, TimeZone object) {
        toSerialization(jsonWriter, object);
        return jsonWriter;
    }

    /**
     * @param jsonWriter
     * @param object
     * @return jsonWriter
     */
    @Override
    public <W extends SerializationWriter> W toSerialization(W serializationWriter, TimeZone object) {
        String timezoneId = object.getID();
        return serializationWriter.value(timezoneId);
    }

    /**
     * @see org.amplafi.flow.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, java.lang.Object[])
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public <K> K fromSerialization(Class<K> clazz, Object value, Object... parameters) {
        if ( value == null ) {
            return null;
        } else {
            return (K) TimeZone.getTimeZone(value.toString());
        }
    }

    /**
     * @see org.amplafi.flow.json.JsonRenderer#getClassToRender()
     */
    @Override
    public Class<? extends TimeZone> getClassToRender() {
        return TimeZone.class;
    }

    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(FlowPropertyProvider , org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition, java.lang.Object)
     */
    @Override
    protected TimeZone doDeserialize(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        return (TimeZone) fromSerialization(dataClassDefinition.getDataClass(), serializedObject);
    }

}
