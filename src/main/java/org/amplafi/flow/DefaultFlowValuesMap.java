package org.amplafi.flow;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;

public class DefaultFlowValuesMap implements FlowValuesMap, Serializable {

    private Map<DefaultFlowValuesMapKey, String> map;

    public DefaultFlowValuesMap() {
        this.map = Collections.synchronizedMap(new LinkedHashMap<DefaultFlowValuesMapKey, String>());
    }
    public DefaultFlowValuesMap(Map<?,?> initialFlowState) {
        this();
        if ( MapUtils.isNotEmpty(initialFlowState)) {
            for(Map.Entry entry: initialFlowState.entrySet()) {
                Object value = entry.getValue();
                if ( value != null) {
                    DefaultFlowValuesMapKey key = DefaultFlowValuesMapKey.toKey(entry.getKey());
                    this.map.put(key, value.toString());
                }
            }
        }
    }
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(toKey(null, key));
    }

    @Override
    public String get(Object key) {
        String value = map.get(toKey(null, key));
        return value;
    }

    @Override
    public String get(Object namespace, Object key) {
        return map.get(toKey(namespace, key));
    }

    protected DefaultFlowValuesMapKey toKey(Object namespace, Object key) {
        return new DefaultFlowValuesMapKey(namespace, key);
    }
    @Override
    public Map<String, String> getAsFlattenedStringMap() {
        return getAsStringMap(false, true);
    }

    @Override
    public Map<String, String> getAsStringMap(boolean trimBlanks, boolean preserveNamespace) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for(Map.Entry<DefaultFlowValuesMapKey, String> entry: map.entrySet()) {
            if ( !trimBlanks || isNotBlank(entry.getValue())) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public String put(Object flowActivityName, Object key, Object value) {
        return this.map.put(toKey(flowActivityName, key), (String)value);
    }
    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(FlowValueMapKey key, CharSequence value) {
        return this.map.put(toKey(null, key), ObjectUtils.toString(value, null));
    }
    public String put(Object key, Object value) {
        return this.map.put(toKey(null, key), ObjectUtils.toString(value, null));
    }
    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        this.map.clear();
    }
    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }
    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<CharSequence, String>> entrySet() {
        Set s = this.map.entrySet();
        return s;
    }
    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<CharSequence> keySet() {
        Set keySet = this.map.keySet();
        return keySet;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map m) {
        Set<Map.Entry<Object, Object>> entrySet = m.entrySet();
        for(Map.Entry<Object, Object> entry: entrySet) {
            this.put(entry.getKey(), entry.getValue());
        }
    }
    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public String remove(Object key) {
        return this.map.remove(DefaultFlowValuesMapKey.toKey(key));
    }
    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return this.map.size();
    }
    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<String> values() {
        return this.map.values();
    }
    @Override
    public String toString() {
        return ObjectUtils.toString(map);
    }
}
