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

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.JSONObject;
import org.amplafi.json.JSONStringer;
import org.amplafi.json.JSONWriter;
import org.apache.commons.lang.ObjectUtils;

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
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition , java.lang.Object)
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
