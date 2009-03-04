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
import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonSelfRenderer;


/**
 * @author patmoore
 *
 */
public class JsonSelfRendererFlowTranslator extends AbstractFlowTranslator {

    /**
     * @see org.amplafi.flow.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<JsonSelfRenderer> getTranslatedClass() {
        return JsonSelfRenderer.class;
    }

    /**
     * @see org.amplafi.flow.FlowTranslator#serialize(org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition , org.amplafi.json.JSONWriter, java.lang.Object)
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
