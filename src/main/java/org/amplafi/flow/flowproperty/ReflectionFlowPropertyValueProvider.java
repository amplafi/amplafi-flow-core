/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowPropertyDefinition;

import com.sworddance.beans.BeanWorker;

/**
 * Uses reflection to trace to find the property value.
 * if at any point a null is returned then null is returned (no {@link NullPointerException} will be thrown)
 * @author patmoore
 *
 */
public class ReflectionFlowPropertyValueProvider extends BeanWorker implements FlowPropertyValueProvider<FlowActivity> {

    private Object object;
    public ReflectionFlowPropertyValueProvider(Object object) {
        this.object = object;
    }
    public ReflectionFlowPropertyValueProvider(String... propertyNames) {
        super(propertyNames);
    }
    public ReflectionFlowPropertyValueProvider(Object object, String... propertyNames) {
        super(propertyNames);
        this.object = object;
    }

    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        final Object base = this.object==null?flowActivity:this.object;
        return (T) getValue(base, this.getPropertyName(0));
    }

}
