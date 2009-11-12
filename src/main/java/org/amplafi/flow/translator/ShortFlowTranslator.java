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
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.flow.validation.InconsistencyTracking;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.renderers.NumberJsonRenderer;



public class ShortFlowTranslator extends AbstractFlowTranslator<Short> {

    public ShortFlowTranslator() {
        super(NumberJsonRenderer.INSTANCE);
        this.addSerializedFormClasses(Number.class, int.class, long.class, short.class);
        this.addDeserializedFormClasses(short.class);
    }
    @SuppressWarnings("unused")
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
    public IJsonWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, IJsonWriter jsonWriter, Short object) {
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
    public Short getDefaultObject(FlowPropertyProvider flowPropertyProvider) {
        return 0;
    }
    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition , java.lang.Object)
     */
    @Override
    protected Short doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        throw new UnsupportedOperationException();
    }
}
