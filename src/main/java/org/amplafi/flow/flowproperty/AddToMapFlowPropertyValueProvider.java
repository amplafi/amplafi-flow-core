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

import java.util.LinkedHashMap;
import java.util.Map;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowPropertyDefinition;
import org.apache.commons.collections.MapUtils;
import static com.sworddance.util.CUtilities.*;


/**
 * incrementally construct a map.
 * @param <K>
 * @param <V>
 *
 */
public class AddToMapFlowPropertyValueProvider<K,V> implements ChainedFlowPropertyValueProvider<FlowActivity> {

    private FlowPropertyValueProvider<FlowActivity> previous;
    private Map<K,V> values;
    public AddToMapFlowPropertyValueProvider(V...valuesToAdd) {
        values = new LinkedHashMap<K, V>();
        putAll(values, valuesToAdd);
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
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
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
     * @return the value from the previous {@link org.amplafi.flow.FlowPropertyValueProvider} in the chain of {@link org.amplafi.flow.FlowPropertyValueProvider}
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
     * @see org.amplafi.flow.flowproperty.ChainedFlowPropertyValueProvider#setPrevious(org.amplafi.flow.FlowPropertyValueProvider)
     */
    @Override
    public void setPrevious(FlowPropertyValueProvider previous) {
        this.previous = previous;
    }

    @Override
    public String toString() {
        return getClass()+": adding "+values + " chain: ["+this.previous+"]";
    }

}
