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

/**
 * @author patmoore
 *
 */
public class ClassFlowRenderer implements FlowRenderer<Class<?>> {

    public static final ClassFlowRenderer INSTANCE = new ClassFlowRenderer();

    /**
     * @see org.amplafi.flow.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    public <K> K fromSerialization(Class<K> clazz, Object value, Object... parameters) {
        if ( value == null ) {
            return null;
        } else {
            try {
                return (K) Class.forName(value.toString());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    /**
     * @see org.amplafi.flow.json.JsonRenderer#getClassToRender()
     */
    @SuppressWarnings("unchecked")
    public Class/*<? extends Class<?>>*/ getClassToRender() {
        return /*(Class<? extends Class<?>>)*/ Class.class;
    }

    /**
     * @see org.amplafi.flow.json.JsonRenderer#toJson(org.amplafi.flow.json.IJsonWriter, java.lang.Object)
     */
    public <W extends SerializationWriter> W toSerialization(W serializationWriter, Class<?> o) {
        serializationWriter.value(o.getCanonicalName());
        return serializationWriter;
    }

}
