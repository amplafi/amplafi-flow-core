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
package org.amplafi.flow.flowproperty;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.amplafi.flow.FlowPropertyDefinition;
import org.apache.commons.lang.ObjectUtils;

import com.sworddance.util.ApplicationIllegalStateException;

/**
 * {@link org.amplafi.flow.FlowPropertyValueProvider} that handles statically provided values.
 *
 * Exploring as alternative to {@link FlowPropertyDefinitionImpl#getInitial()}
 * @param <FA>
 *
 */
public class FixedFlowPropertyValueProvider<FA extends FlowPropertyProvider> extends AbstractFlowPropertyValueProvider<FA> {

    private Object defaultObject;
    private static Object NULL = new Object() {
        @Override
        public String toString() {
            return "<null>";
        }
    };
    /**
     * key off of FlowPropertyDefinition so that different FPD can translate defaultObject differently
     */
    private ConcurrentMap<FlowPropertyDefinition, Object> map = new ConcurrentHashMap<FlowPropertyDefinition, Object>();

    @SuppressWarnings("unchecked")
    public FixedFlowPropertyValueProvider(Object defaultObject) {
        super((Class<FA>)FlowPropertyProvider.class);
        this.defaultObject = defaultObject;
    }
    /**
     * @param flowPropertyDefinition
     */
    public void convertable(FlowPropertyDefinition flowPropertyDefinition) {
        ApplicationIllegalStateException.valid( flowPropertyDefinition.getDataClassDefinition().isDeserializable(flowPropertyDefinition, this.defaultObject),
            this, " cannot convert value=", this.defaultObject);
    }

    @Override
    @SuppressWarnings({  "unchecked" })
    public <T> T get(FA flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        T value;
        if ( !map.containsKey(flowPropertyDefinition) ) {
            convertable(flowPropertyDefinition);
            value = (T) flowPropertyDefinition.getDataClassDefinition().deserialize(flowPropertyProvider, flowPropertyDefinition, this.defaultObject);
            if ( value == null ) {
                map.putIfAbsent(flowPropertyDefinition, NULL);
            } else {
                map.putIfAbsent(flowPropertyDefinition, value);
            }
        }
        value = (T) map.get(flowPropertyDefinition);
        if ( value == NULL) {
            value = null;
        }
        return value;
    }
    /**
     * @return the value as a string.
     */
    public String getDefaultString() {
        return ObjectUtils.toString(defaultObject);
    }

    public Class<?> getSuggestedClass() {
        return this.defaultObject == null? String.class:this.defaultObject.getClass();
    }

    @Override
    public int hashCode() {
        return this.defaultObject == null?1:this.defaultObject.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if ( o instanceof FixedFlowPropertyValueProvider<?>) {
            return ObjectUtils.equals(this.defaultObject, ((FixedFlowPropertyValueProvider<?>)o).defaultObject);
        } else {
            return ObjectUtils.equals(this.defaultObject, o);
        }
    }

    @Override
    public String toString() {
        return "FixedProperty value="+getDefaultString();
    }
}
