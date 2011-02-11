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

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationIllegalStateException;

import org.apache.commons.lang.ObjectUtils;

/**
 * {@link org.amplafi.flow.FlowPropertyValueProvider} that handles statically provided values.
 *
 * Exploring as alternative to {@link FlowPropertyDefinitionImpl#getInitial()}
 * @param <FA>
 *
 */
public class FixedFlowPropertyValueProvider<FA extends FlowPropertyProvider> extends AbstractFlowPropertyValueProvider<FA> {

    private final Object defaultObject;
    private static Object NULL = new Object() {
        @Override
        public String toString() {
            return "<null>";
        }
    };

    /**
     * Used when want to force a {@link FlowPropertyDefinition} to not have the default {@link FlowPropertyValueProvider} assignments happen.
     */
    public static FixedFlowPropertyValueProvider<FlowPropertyProvider> NULL_INSTANCE = new FixedFlowPropertyValueProvider<FlowPropertyProvider>(NULL);
    /**
     * key off of FlowPropertyDefinition so that different FPD can translate defaultObject differently
     */
    private ConcurrentMap<FlowPropertyDefinition, Object> map = new ConcurrentHashMap<FlowPropertyDefinition, Object>();

    @SuppressWarnings("unchecked")
    public FixedFlowPropertyValueProvider(Object defaultObject) {
        super((Class<FA>)FlowPropertyProvider.class);
        ApplicationIllegalArgumentException.notNull(defaultObject, "Fixed value cannot be null. If explicit null is really intended use FixedFlowPropertyValueProvider.NULL_INSTANCE");
        this.defaultObject = defaultObject;
    }
    /**
     * @param flowPropertyDefinition
     */
    public void convertable(FlowPropertyDefinition flowPropertyDefinition) {
        DataClassDefinition dataClassDefinition = flowPropertyDefinition.getDataClassDefinition();
        ApplicationIllegalStateException.checkState( dataClassDefinition.isDeserializable(flowPropertyDefinition, this.getDefaultObject()),
            this, " cannot convert value=", this.getDefaultObject());
    }

    @Override
    @SuppressWarnings({  "unchecked" })
    public <T> T get(FA flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        T value;
        if ( !map.containsKey(flowPropertyDefinition) ) {
            convertable(flowPropertyDefinition);
            value = (T) flowPropertyDefinition.getDataClassDefinition().deserialize(flowPropertyProvider, flowPropertyDefinition, this.getDefaultObject());
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
        return ObjectUtils.toString(getDefaultObject());
    }

    public Class<?> getSuggestedClass() {
        return this.getDefaultObject() == null? String.class:this.getDefaultObject().getClass();
    }

    @Override
    public int hashCode() {
        return this.getDefaultObject() == null?1:this.getDefaultObject().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if ( o instanceof FixedFlowPropertyValueProvider<?>) {
            return ObjectUtils.equals(this.getDefaultObject(), ((FixedFlowPropertyValueProvider<?>)o).getDefaultObject());
        } else {
            return ObjectUtils.equals(this.getDefaultObject(), o);
        }
    }
    /**
     * @return the defaultObject
     */
    private Object getDefaultObject() {
        return defaultObject == NULL?null:this.defaultObject;
    }
    @Override
    public String toString() {
        return "FixedProperty value="+getDefaultString();
    }
    /**
     * @param <FPP>
     * @param testAndConfigFlowPropertyDefinition
     * @param defaultObject
     * @param flowPropertyDefinition
     * @return
     */
    public static <FPP extends FlowPropertyProvider> FixedFlowPropertyValueProvider<FPP> newFixedFlowPropertyValueProvider(Object defaultObject,
        FlowPropertyDefinitionImpl flowPropertyDefinition, boolean testAndConfigFlowPropertyDefinition) {
        FixedFlowPropertyValueProvider<FPP> fixedFlowPropertyValueProvider = null;
        if ( defaultObject != null ) {
            fixedFlowPropertyValueProvider = new FixedFlowPropertyValueProvider<FPP>(defaultObject);
            fixedFlowPropertyValueProvider.convertable(flowPropertyDefinition);
            if ( testAndConfigFlowPropertyDefinition ) {
                DataClassDefinition dataClassDefinition = flowPropertyDefinition.getDataClassDefinition();
                if (dataClassDefinition.isDataClassDefined()) {
                    if (!dataClassDefinition.getDataClass().isPrimitive()) {
                        // really need to handle the autobox issue better.
                        dataClassDefinition.getDataClass().cast(defaultObject);
                    }
                } else {
                    dataClassDefinition.setDataClass(defaultObject.getClass());
                }
            }
        }
        return fixedFlowPropertyValueProvider;
    }
}
