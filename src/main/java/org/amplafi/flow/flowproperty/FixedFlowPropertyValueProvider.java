/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.apache.commons.lang.ObjectUtils;

/**
 * {@link org.amplafi.flow.FlowPropertyValueProvider} that handles statically provided values.
 * @param <FA>
 *
 */
public class FixedFlowPropertyValueProvider<FA extends FlowActivity> extends AbstractFlowPropertyValueProvider<FA> {

    private Object defaultObject;
    private static Object NULL = new Object();
    /**
     * key off of FlowPropertyDefinition so that different FPD can translate defaultObject differently
     */
    private ConcurrentMap<FlowPropertyDefinition, Object> map = new ConcurrentHashMap<FlowPropertyDefinition, Object>();

    public FixedFlowPropertyValueProvider(Object defaultObject) {
        this.defaultObject = defaultObject;
    }
    /**
     * @param flowPropertyDefinition
     * @param value
     */
    @SuppressWarnings("unchecked")
    private <T> T getDefaultValue(FlowPropertyDefinition flowPropertyDefinition) {
        T value;
        if ( !map.containsKey(flowPropertyDefinition) ) {
            convertable(flowPropertyDefinition);
            value = (T) flowPropertyDefinition.getDataClassDefinition().deserialize(flowPropertyDefinition, this.defaultObject);
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
     * @param flowPropertyDefinition
     */
    public void convertable(FlowPropertyDefinition flowPropertyDefinition) {
        if ( !flowPropertyDefinition.getDataClassDefinition().isDeserializable(flowPropertyDefinition, this.defaultObject)) {
            throw new IllegalStateException(this + " cannot convert value="+ this.defaultObject);
        }
    }
    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
     */
    @Override
    @SuppressWarnings({ "unused", "unchecked" })
    public <T> T get(FA flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        return (T) this.getDefaultValue(flowPropertyDefinition);
    }
    /**
     * @return
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
        if ( o instanceof FixedFlowPropertyValueProvider) {
            return ObjectUtils.equals(this.defaultObject, ((FixedFlowPropertyValueProvider)o).defaultObject);
        } else {
            return ObjectUtils.equals(this.defaultObject, o);
        }
    }

}
