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
public class AddToListFlowPropertyValueProvider<V> extends AbstractChainedFlowPropertyValueProvider<FlowPropertyProvider> {

    private List<V> values;
    public AddToListFlowPropertyValueProvider(V...valuesToAdd) {
        super(FlowPropertyProvider.class);
        this.values = Arrays.asList(valuesToAdd);
    }

    /**
     *
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.flowproperty.FlowPropertyProvider, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        List<V> result = getPropertyFromChain(flowPropertyProvider, flowPropertyDefinition);
        if ( result == null) {
            result = new ArrayList<V>();
        }
        if ( isNotEmpty(values)) {
            result.addAll(values);
        }
        return (T) result;
    }

	@Override
	public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
		// not checking so sure.
		return true;
	}

}
