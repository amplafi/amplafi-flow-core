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
public interface FlowValuesMap<K extends FlowValueMapKey, V extends CharSequence> extends Map<K, V> {

    V get(Object key);

    V get(Object namespace, Object key);

    V put(Object namespace, Object key, Object value);

    V put(K key, V value);

    boolean containsKey(Object key);

    boolean isEmpty();

    Map<String, String> getAsFlattenedStringMap();

    Map<String, String> getAsStringMap(boolean trimEmptyBlank, boolean preserveNamespace);

}
