package org.amplafi.flow.translator;


import java.util.LinkedHashMap;
import java.util.Map;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.FlowPropertyDefinition;
import org.amplafi.json.JSONObject;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.renderers.MapJsonRenderer;
import org.apache.commons.collections.MapUtils;


/**
 * {@link FlowTranslator} that serializes/deserializes {@link Map}s for flows.
 * @author patmoore
 *
 * @param <K>
 * @param <V>
 */
public class MapFlowTranslator<K,V> extends AbstractFlowTranslator<Map<? extends K, ? extends V>> {
    private Class<?> translatedClass = Map.class;
    private Class<?> defaultObjectClass = LinkedHashMap.class;
    public MapFlowTranslator() {
        super(new MapJsonRenderer());
        this.addSerializedFormClasses(JSONObject.class);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Map<? extends K, ? extends V> doDeserialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, Object serializedObject) {
        JSONObject jsonObject = JSONObject.toJsonObject(serializedObject);
        Map<K, V> map = new LinkedHashMap<K, V>();
        for(String key : jsonObject.keys() ) {
            Object object = jsonObject.get(key);
            DataClassDefinition keyDataClassDefinition = dataClassDefinition.getKeyDataClassDefinition();
            K realKey = (K) keyDataClassDefinition.deserialize(flowPropertyDefinition, key);
            DataClassDefinition elementDataClassDefinition = dataClassDefinition.getElementDataClassDefinition();
            V realValue = (V) elementDataClassDefinition.deserialize(flowPropertyDefinition, object);
            map.put(realKey, realValue);
        }
        return map;
    }

    @Override
    public JSONWriter doSerialize(FlowPropertyDefinition flowPropertyDefinition, DataClassDefinition dataClassDefinition, JSONWriter jsonWriter, Map<? extends K, ? extends V> map) {
        jsonWriter.object();
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
        return jsonWriter.endObject();
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
    public Map<? extends K, ? extends V> getDefaultObject(FlowActivity flowActivity) {
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
