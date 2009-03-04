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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowPropertyDefinition;

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
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
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
     * @return the value from the previous {@link org.amplafi.flow.FlowPropertyValueProvider} in the chain of {@link FlowPropertyValueProvider}
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

}
