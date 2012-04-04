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
package org.amplafi.flow.impl;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.amplafi.flow.FlowValuesMap;
import org.amplafi.flow.FlowValueMapKey;

import static com.sworddance.util.CUtilities.*;

public class DefaultFlowValuesMap implements FlowValuesMap<FlowValueMapKey, String>, Serializable {

    private Map<DefaultFlowValuesMapKey, String> map;

    public DefaultFlowValuesMap() {
        this.map = Collections.synchronizedMap(new LinkedHashMap<DefaultFlowValuesMapKey, String>());
    }
    public DefaultFlowValuesMap(Map<?,?> initialFlowState) {
        this(initialFlowState, null);
    }
    public DefaultFlowValuesMap(FlowValuesMap<FlowValueMapKey, CharSequence> initialFlowState) {
        this();
        if ( isNotEmpty(initialFlowState)) {
            for(Map.Entry entry: initialFlowState.entrySet()) {
                Object value = entry.getValue();
                if ( value != null) {
                    DefaultFlowValuesMapKey key = DefaultFlowValuesMapKey.toKey(entry.getKey());
                    this.map.put(key, value.toString());
                }
            }
        }
    }
    public DefaultFlowValuesMap(Map<?, ?> initialFlowState, String flowLookupKey) {
    	this();
        if ( isNotEmpty(initialFlowState)) {
            for(Map.Entry entry: initialFlowState.entrySet()) {
                Object value = entry.getValue();
                if ( value != null) {
                    DefaultFlowValuesMapKey key = new DefaultFlowValuesMapKey(flowLookupKey, entry.getKey());
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
    public boolean containsKey(Object namespace, Object key) {
        return map.containsKey(toKey(namespace, key));
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
    public String remove(Object namespace, Object key) {
        return map.remove(toKey(namespace, key));
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
    public String put(Object namespace, Object key, Object value) {
        return this.map.put(toKey(namespace, key), (String)value);
    }
    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public String put(FlowValueMapKey key, String value) {
        return this.map.put(toKey(null, key), ObjectUtils.toString(value, null));
    }

    public String putAny(Object key, Object value) {
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
    @SuppressWarnings("unchecked")
    @Override
    public Set<java.util.Map.Entry<FlowValueMapKey, String>> entrySet() {
        Set s = this.map.entrySet();
        return s;
    }
    /**
     * @see java.util.Map#keySet()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<FlowValueMapKey> keySet() {
        Set keySet = this.map.keySet();
        return keySet;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map m) {
        Set<Map.Entry<Object, Object>> entrySet = m.entrySet();
        for(Map.Entry<Object, Object> entry: entrySet) {
            this.putAny(entry.getKey(), entry.getValue());
        }
    }

    public void putAll(Object namespace, Map<?,?> subMap) {
        if (isNotEmpty(subMap)) {
            for (Map.Entry<?, ?> entry : subMap.entrySet()) {
                this.put(namespace, entry.getKey(), entry.getValue());
            }
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
