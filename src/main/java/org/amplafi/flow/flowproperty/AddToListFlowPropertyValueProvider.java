/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.amplafi.flow.FlowActivity;


import static org.apache.commons.collections.CollectionUtils.*;
/**
 * Adds the values to the list that is stored in a property.
 *
 * Used when many different FlowActivities may want to add values to a list that is stored in a single property.
 * @param <V>
 * @author patmoore
 *
 */
public class AddToListFlowPropertyValueProvider<V> implements ChainedFlowPropertyValueProvider<FlowActivity> {

    private FlowPropertyValueProvider<FlowActivity> previous;
    private List<V> values;
    public AddToListFlowPropertyValueProvider(V...valuesToAdd) {
        this.values = Arrays.asList(valuesToAdd);
    }
    /**
     * @see org.amplafi.flow.flowproperty.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.flowproperty.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        List<V> result = getPreviousGet(flowActivity, flowPropertyDefinition);
        if ( result == null) {
            result = new ArrayList<V>();
        }
        if ( isNotEmpty(values)) {
            result.addAll(values);
        }
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

}
