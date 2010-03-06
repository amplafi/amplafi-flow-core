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
package org.amplafi.flow;

import java.util.Map;

import com.sworddance.core.Emptyable;

/**
 * implementers will store values for the {@link FlowState}.
 *
 * Possible implementers include classes that map to a db table.
 *
 * Roughly follows the java.util.Map definition - so respect
 * that interface's conventions.
 * @author Patrick Moore
 * @param <K> extends {@link FlowValueMapKey} the key class.
 * @param <V> extends CharSequence the value class.
 *
 */
public interface FlowValuesMap<K extends FlowValueMapKey, V extends CharSequence> extends Emptyable, Map<K,V> {

    /**
     * FlowValueMap is expected to do any needed conversions to the FlowValueMapKey used (? what about put operations ? )
     * @see java.util.Map#get(java.lang.Object)
     */
    V get(Object key);

    V get(Object namespace, Object key);

    /**
     *
     * @param namespace
     * @param key
     * @param value
     * @return the previous object at the (namespace,key) value.
     */
    V put(Object namespace, Object key, Object value);

    /**
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    V put(K key, V value);
    /**
     * Removes the key.
     * @param namespace
     * @param key
     * @return the object formerly at the key.
     */
    public V remove(Object namespace, Object key) ;

    /**
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    boolean containsKey(Object key);

    /**
     * @param namespace
     * @param key
     * @return true if the FlowValuesMap contains a value with namespace and key.
     */
    boolean containsKey(Object namespace, Object key);

    /**
     *
     * @return a simple map. Namespace will be combined with the key to make the key.
     */
    Map<String, String> getAsFlattenedStringMap();

    Map<String, String> getAsStringMap(boolean trimEmptyBlank, boolean preserveNamespace);

    int size();

}
