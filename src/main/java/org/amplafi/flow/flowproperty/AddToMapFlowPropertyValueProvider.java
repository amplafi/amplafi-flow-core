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

import org.amplafi.flow.FlowPropertyDefinition;
import static com.sworddance.util.CUtilities.*;


/**
 * incrementally construct a map.
 * @param <FPP>
 * @param <K>
 * @param <V>
 *
 */
public class AddToMapFlowPropertyValueProvider<FPP extends FlowPropertyProvider, K,V> extends AbstractChainedFlowPropertyValueProvider<FPP> {

    private Map<K,V> values;
    @SuppressWarnings("unchecked")
    public AddToMapFlowPropertyValueProvider(V...valuesToAdd) {
        super((Class<FPP>) FlowPropertyProvider.class);
        values = new LinkedHashMap<K, V>();
        putAll(values, valuesToAdd);
    }
    @SuppressWarnings("unchecked")
    public AddToMapFlowPropertyValueProvider(K key, V valueToAdd) {
        super((Class<FPP>) FlowPropertyProvider.class);
        values = new LinkedHashMap<K, V>();
        values.put(key, valueToAdd);
    }
    @SuppressWarnings("unchecked")
    public AddToMapFlowPropertyValueProvider(Map<K,V>valuesToAdd) {
        super((Class<FPP>) FlowPropertyProvider.class);
        values = new LinkedHashMap<K, V>();
        if( valuesToAdd != null) {
            values.putAll(valuesToAdd);
        }
    }
    /**
     *
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.flowproperty.FlowPropertyProvider, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FPP flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        Map<K,V> result = getPropertyFromChain(flowPropertyProvider, flowPropertyDefinition);
        if ( result == null) {
            result = new LinkedHashMap<K,V>();
        }
        result.putAll(values);
        return (T) result;
    }
    @Override
    public String toString() {
        return getClass()+": adding "+values + " chain: ["+this.getPrevious()+"]";
    }

}
