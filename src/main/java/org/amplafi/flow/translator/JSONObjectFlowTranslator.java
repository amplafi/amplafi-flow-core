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

import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONObject;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.validation.FlowValidationException;

public class JSONObjectFlowTranslator extends AbstractFlowTranslator<JSONObject> {
    @Override
    public Class<?> getTranslatedClass() {
        return JSONObject.class;
    }

    @Override
    @SuppressWarnings("unused")
    protected IJsonWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition,
        IJsonWriter jsonWriter, JSONObject object) {
        // empty json are appended as null but here we want to preserve them, so write them as {}
        if (object!=null && object.equals(null)) {
            jsonWriter.object();
            jsonWriter.endObject();
            return jsonWriter;
        }
        return jsonWriter.value(object);
    }

    @Override
    @SuppressWarnings("unused")
    protected JSONObject doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition,
                                       Object serializedObject) throws FlowValidationException {
        return JSONObject.toJsonObject(serializedObject);
    }
}
