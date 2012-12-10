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


import java.util.LinkedHashMap;
import java.util.Map;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONObject;
import org.amplafi.json.renderers.MapJsonRenderer;
import org.apache.commons.collections.MapUtils;

import com.sworddance.util.ApplicationIllegalStateException;


/**
 * {@link org.amplafi.flow.translator.FlowTranslator} that serializes/deserializes {@link Map}s for flows.
 * @author patmoore
 *
 * @param <K>
 * @param <V>
 */
public class MapFlowTranslator<K,V> extends AbstractFlowTranslator<Map<? extends K, ? extends V>> {
    private Class<?> translatedClass;
    private Class<?> defaultObjectClass;
    public MapFlowTranslator(Class<? extends Map>translatedClass, Class<? extends Map>defaultObjectClass) {
        super(new MapJsonRenderer());
        this.addSerializedFormClasses(JSONObject.class);
        this.setDefaultObjectClass(defaultObjectClass);
        this.setTranslatedClass(translatedClass);
    }
    public MapFlowTranslator() {
        this(Map.class, LinkedHashMap.class);
        this.addSerializedFormClasses(JSONObject.class);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Map<? extends K, ? extends V> doDeserialize(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        JSONObject jsonObject = JSONObject.toJsonObject(serializedObject);
        Map<K, V> map;
        try {
            map = (Map<K, V>) defaultObjectClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ApplicationIllegalStateException(e);
        }
        for(String key : jsonObject.keys() ) {
            Object object = jsonObject.get(key);
            DataClassDefinition keyDataClassDefinition = dataClassDefinition.getKeyDataClassDefinition();
            K realKey = (K) keyDataClassDefinition.deserialize(flowPropertyProvider, flowPropertyDefinition, key);
            DataClassDefinition elementDataClassDefinition = dataClassDefinition.getElementDataClassDefinition();
            V realValue = (V) elementDataClassDefinition.deserialize(flowPropertyProvider, flowPropertyDefinition, object);
            map.put(realKey, realValue);
        }
        return map;
    }

    @Override
    public IJsonWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, IJsonWriter jsonWriter, Map<? extends K, ? extends V> map) {
        jsonWriter.object();
        try {
            if ( MapUtils.isNotEmpty(map) ) {
                for(Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();
                    if ( key != null && value != null) {
                        DataClassDefinition keyDataClassDefinition = dataClassDefinition.getKeyDataClassDefinition();
                        jsonWriter.key(keyDataClassDefinition.serialize(flowPropertyDefinition, key).toString());
                        dataClassDefinition.getElementDataClassDefinition().serialize(flowPropertyDefinition, jsonWriter, value);
                    }
                }
            }
        } finally {
            jsonWriter.endObject();
        }
        return jsonWriter;
    }

    @Override
    public boolean isAssignableFrom(Class<?> differentClass) {
        if ( getTranslatedClass().isAssignableFrom(differentClass)) {
            return true;
        } else if (CharSequence.class.isAssignableFrom(differentClass)) {
            return true;
        } else {
            return JSONObject.class.isAssignableFrom(differentClass);
        }
    }
    @Override
    public Map<? extends K, ? extends V> getDefaultObject(FlowPropertyProvider flowPropertyProvider) {
        try {
            return (Map<? extends K, ? extends V>) getDefaultObjectClass().newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Class<?> getTranslatedClass() {
        return this.translatedClass;
    }
    /**
     * @param translatedClass the translatedClass to set
     */
    public void setTranslatedClass(Class<?> translatedClass) {
        this.translatedClass = translatedClass;
    }
    /**
     * @param defaultObjectClass the defaultObjectClass to set
     */
    public void setDefaultObjectClass(Class<?> defaultObjectClass) {
        this.defaultObjectClass = defaultObjectClass;
    }
    /**
     * @return the defaultObjectClass
     */
    public Class<?> getDefaultObjectClass() {
        return defaultObjectClass;
    }

}
