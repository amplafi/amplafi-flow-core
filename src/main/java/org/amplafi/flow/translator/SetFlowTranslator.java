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

import java.util.LinkedHashSet;
import java.util.Set;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.renderers.IterableJsonOutputRenderer;


public class SetFlowTranslator<T> extends FlowCollectionTranslator<Set<? extends T>, T> {

    public SetFlowTranslator() {
        super(new IterableJsonOutputRenderer<Set<? extends T>>(false));
    }
    @Override
    public Set<? extends T> deserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serialized) {
        if ( serialized != null) {
            Set<T> set = new LinkedHashSet<T>();
            super.deserialize(flowPropertyDefinition, dataClassDefinition, set, serialized);
            return set;
        } else {
            return null;
        }
    }

    @Override
    public IJsonWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, IJsonWriter jsonWriter, Set<? extends T> object) {
        return super.doSerialize(flowPropertyDefinition, dataClassDefinition, jsonWriter, object);
    }

    @Override
    public Class<?> getTranslatedClass() {
        return Set.class;
    }

    @Override
    public Set<? extends T> getDefaultObject(FlowActivity flowActivity) {
        return new LinkedHashSet<T>();
    }

}
