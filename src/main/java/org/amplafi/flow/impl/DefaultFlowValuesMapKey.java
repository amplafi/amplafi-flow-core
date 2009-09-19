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

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

import static org.apache.commons.lang.StringUtils.*;
import org.amplafi.flow.FlowValueMapKey;

/**
 * Avoids string concatenation performance issues.
 * @author Patrick Moore
 *
 */
public class DefaultFlowValuesMapKey implements FlowValueMapKey, Serializable {
    private String namespace;
    private String key;
    private String stringValue;

    public DefaultFlowValuesMapKey(Object namespace, Object key) {
        String space = ObjectUtils.toString(namespace, null);
        setNamespace(space);
        this.key = ObjectUtils.toString(key, null);
    }

    public DefaultFlowValuesMapKey(CharSequence key) {
        String[] strings = key.toString().split(NAMESPACE_SEPARATOR);
        if (strings.length == 1) {
            this.setNamespace(NO_NAMESPACE);
            this.key = strings[0];
        } else if ( strings.length > 1) {
            this.setNamespace(strings[0]);
            this.key = strings[1];
        }
    }
    public DefaultFlowValuesMapKey(FlowValueMapKey key) {
        this.setNamespace(key.getNamespace());
        this.key = key.getKey();
    }
    public static DefaultFlowValuesMapKey toKey(Object key) {
        if ( key == null ) {
            return null;
        } else if ( key instanceof DefaultFlowValuesMapKey) {
            return (DefaultFlowValuesMapKey) key;
        } else if ( key instanceof FlowValueMapKey) {
            return new DefaultFlowValuesMapKey((FlowValueMapKey)key);
        } else {
            return new DefaultFlowValuesMapKey(key.toString());
        }
    }

    /**
     * @param space
     */
    private void setNamespace(String space) {
        this.namespace = isNotBlank(space)?space.trim():NO_NAMESPACE;
    }
    @Override
    public boolean equals(Object object) {
        if (object instanceof DefaultFlowValuesMapKey) {
            DefaultFlowValuesMapKey other = (DefaultFlowValuesMapKey) object;
            return ObjectUtils.equals(other.key, key) && ObjectUtils.equals(other.namespace, namespace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        if ( namespace != null ) {
            result = namespace.hashCode();
        } else {
            result = 0;
        }
        if ( key != null ) {
            result ^= key.hashCode();
        }
        return result;
    }
    @Override
    public String toString() {
        if ( stringValue == null ) {
            if ( isNotBlank(namespace)) {
                stringValue = namespace +NAMESPACE_SEPARATOR + ObjectUtils.toString(key);
            } else {
                stringValue = ObjectUtils.toString(key);
            }
        }
        return stringValue;
    }
    /**
     * @see java.lang.CharSequence#charAt(int)
     */
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    /**
     * @see java.lang.CharSequence#length()
     */
    @Override
    public int length() {
        return toString().length();
    }

    /**
     * @see java.lang.CharSequence#subSequence(int, int)
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /**
     * @see org.amplafi.flow.FlowValueMapKey#getKey()
     */
    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * @see org.amplafi.flow.FlowValueMapKey#getNamespace()
     */
    @Override
    public String getNamespace() {
        return this.namespace;
    }
}