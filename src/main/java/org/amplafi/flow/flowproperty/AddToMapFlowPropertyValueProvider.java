/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.LinkedHashMap;
import java.util.Map;

import org.amplafi.flow.FlowActivity;
import org.apache.commons.collections.MapUtils;


/**
 * incrementally construct a map.
 * @param <K>
 * @param <V>
 *
 */
public class AddToMapFlowPropertyValueProvider<K,V> implements ChainedFlowPropertyValueProvider<FlowActivity> {

    private FlowPropertyValueProvider<FlowActivity> previous;
    private Map<K,V> values;
    @SuppressWarnings("unchecked")
    public AddToMapFlowPropertyValueProvider(V...valuesToAdd) {
        values = new LinkedHashMap<K, V>();
        for (V value: valuesToAdd) {
            values.put(((MapKeyProvider<K>)value).getKey(), value);
        }
    }
    public AddToMapFlowPropertyValueProvider(K key, V valueToAdd) {
        values = new LinkedHashMap<K, V>();
        values.put(key, valueToAdd);
    }
    public AddToMapFlowPropertyValueProvider(Map<K,V>valuesToAdd) {
        values = new LinkedHashMap<K, V>();
        if( MapUtils.isNotEmpty(valuesToAdd)) {
            values.putAll(valuesToAdd);
        }
    }
    /**
     * @see org.amplafi.flow.flowproperty.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.flowproperty.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        Map<K,V> result = getPreviousGet(flowActivity, flowPropertyDefinition);
        if ( result == null) {
            result = new LinkedHashMap<K,V>();
        }
        result.putAll(values);
        return (T) result;
    }
    /**
     * @param flowActivity
     * @param flowPropertyDefinition
     * @return the value from the previous {@link FlowPropertyValueProvider} in the chain of {@link FlowPropertyValueProvider}
     */
    @SuppressWarnings("unchecked")
    protected <T> T getPreviousGet(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        T result = null;
        if ( previous != null ) {
            result = (T) previous.get(flowActivity, flowPropertyDefinition);
        }
        return result;
    }
    /**
     * @see org.amplafi.flow.flowproperty.ChainedFlowPropertyValueProvider#setPrevious(org.amplafi.flow.flowproperty.FlowPropertyValueProvider)
     */
    @Override
    public void setPrevious(FlowPropertyValueProvider previous) {
        this.previous = previous;
    }

    @Override
    public String toString() {
        return getClass()+": adding "+values + " chain: ["+this.previous+"]";
    }

    public static interface MapKeyProvider<K> {
        K getKey();
    }
}
