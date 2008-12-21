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
