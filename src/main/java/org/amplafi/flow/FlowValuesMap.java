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
 *
 * Keys are federated (i.e. namespace.key) ({@link FlowValueMapKey})
 *
 * Federated keys allow each flow INSTANCE and each flowActivity INSTANCE to have a unique namespace.
 * Flows can call other flows and need to know that other flows in the call stack will not interfer with
 * the caller flows values.
 * When calling another flow, the calling flow needs to pass values to the called flows. It is too burdensome / impossible for the flow that is setting the values
 * to know the namespace of the flow reading the value.
 *
 * For example, a flow may create an object putting its db id in the flowstate and then terminate, but the flow that will read the new object's id to access the new object is not determined
 * by the flow that created the object.
 *
 * The global namespace ( i.e. keys without a namespace ) is the scratch area where flows communicate.
 *
 * When a flow is started, it looks through the global namespace for keys that match the keys in the FlowPropertyDefinitions attached to the flow or the flow's activities.
 * Matching key value pairs are moved to the flow's unique name space.
 *
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
    @Override
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
    * @param namespace
     * @param subMap
    */
    void putAll(Object namespace, Map<?,?>subMap);
    /**
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    V put(K key, V value);
    /**
     * Removes the key.
     * @param namespace
     * @param key
     * @return the object formerly at the key.
     */
    public V removeFromNamespace(Object namespace, Object key) ;

    /**
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
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

    @Override
    int size();

}
