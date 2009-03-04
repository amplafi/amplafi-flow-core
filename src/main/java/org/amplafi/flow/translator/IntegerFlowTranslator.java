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

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.InconsistencyTracking;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.renderers.NumberJsonRenderer;



public class IntegerFlowTranslator extends AbstractFlowTranslator<Integer> {

    public IntegerFlowTranslator() {
        super(NumberJsonRenderer.INSTANCE);
        this.addSerializedFormClasses(Number.class, int.class, long.class, short.class, Integer.class);
        this.addDeserializedFormClasses(int.class);
    }
    @SuppressWarnings("unused")
    @Override
    protected Integer doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        if ( serializedObject instanceof Number) {
            return new Integer(((Number)serializedObject).intValue());
        }
        String s = serializedObject.toString();
        try {
            return new Integer(s);
        } catch(NumberFormatException e) {
            throw new FlowValidationException(new InconsistencyTracking("cannot-be-parsed",
                    s+": contains non-numerics"));
        }
    }

    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, Integer object) {
        return jsonWriter.value(object);
    }

    @Override
    public Class<Integer> getTranslatedClass() {
        return Integer.class;
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
    public Integer getDefaultObject(FlowActivity flowActivity) {
        return 0;
    }
}
