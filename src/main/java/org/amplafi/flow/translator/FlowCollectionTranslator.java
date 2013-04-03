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

import java.util.Collection;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.validation.FlowValidationException;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONArray;
import org.amplafi.json.JsonRenderer;
import org.apache.commons.lang.ObjectUtils;


/**
 * Base class to handle collections.
 * @param <C>
 * @param <T>
 */
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
    protected void deserialize(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Collection<T> collection, Object serialized) {
        JSONArray jsonArray;
        if ( serialized instanceof JSONArray) {
            jsonArray = (JSONArray) serialized;
        } else {
            jsonArray = new JSONArray(ObjectUtils.toString(serialized, null));
        }
        for(Object o : jsonArray.asList()) {
            T element= (T) dataClassDefinition.getElementDataClassDefinition().deserialize(flowPropertyProvider, flowPropertyDefinition, o);
            collection.add(element);
        }
    }
    @Override
    public IJsonWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, IJsonWriter jsonWriter, C object) {
        jsonWriter.array();
        try {
            for(T element: object) {
                dataClassDefinition.getElementDataClassDefinition().serialize(flowPropertyDefinition, jsonWriter, element);
            }
        } finally {
            if(jsonWriter.isInArrayMode()) {
                jsonWriter.endArray();
            }
        }
        return jsonWriter;
    }
    /**
     * @see org.amplafi.flow.translator.AbstractFlowTranslator#doDeserialize(FlowPropertyProvider , org.amplafi.flow.FlowPropertyDefinition , org.amplafi.flow.DataClassDefinition, java.lang.Object)
     */
    @Override
    protected C doDeserialize(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) throws FlowValidationException {
        throw new UnsupportedOperationException();
    }

}
