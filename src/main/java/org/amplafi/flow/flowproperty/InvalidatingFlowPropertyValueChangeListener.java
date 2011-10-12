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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import com.sworddance.util.CUtilities;
import com.sworddance.util.NotNullIterator;
import com.sworddance.util.map.ConcurrentInitializedMap;

/**
 * Listen for property changes so that dependent properties can be cleared.
 *
 * The FlowPropertyProvider must implement FlowPropertyProviderWithValues for this listener to work. If not nothing is done in {@link #propertyChange(FlowPropertyProvider, String, FlowPropertyDefinition, String, String)}
 * @author patmoore
 *
 */
public class InvalidatingFlowPropertyValueChangeListener implements FlowPropertyValueChangeListener {

    private ConcurrentMap<String, List<FlowPropertyDefinition>> propertyToDependentPropertiesMap = ConcurrentInitializedMap.<String, FlowPropertyDefinition>newConcurrentInitializedMapWithList();
    /**
     * @see org.amplafi.flow.flowproperty.FlowPropertyValueChangeListener#propertyChange(org.amplafi.flow.flowproperty.FlowPropertyProvider, java.lang.String, org.amplafi.flow.FlowPropertyDefinition, java.lang.String, java.lang.String)
     */
    @Override
    public String propertyChange(FlowPropertyProvider flowPropertyProvider, String namespace, FlowPropertyDefinition flowPropertyDefinition,
        String newValue, String oldValue) {
        if ( flowPropertyProvider instanceof FlowPropertyProviderWithValues) {
            // determine if there are any monitored properties dependent on flowPropertyDefinition.
            Set<String> propertiesToBeInvalidated = new HashSet<String>();
            collectInvalidated(propertiesToBeInvalidated, flowPropertyDefinition);
            for(String propertyToBeInvalidated: NotNullIterator.<String>newNotNullIterator(propertiesToBeInvalidated)) {
                    // clear out the old values.
                ((FlowPropertyProviderWithValues)flowPropertyProvider).setProperty(propertyToBeInvalidated, null);
            }
        }
        return newValue;
    }

    /**
     * @param flowPropertyDefinition
     * @param propertiesToBeInvalidated
     */
    private void collectInvalidated(Set<String> propertiesToBeInvalidated, FlowPropertyDefinition flowPropertyDefinition) {
            // collect set of all properties affected.
        for(String name : flowPropertyDefinition.getAllNames()) {
            if ( this.propertyToDependentPropertiesMap.containsKey(name)) {
                for(FlowPropertyDefinition flowPropertyDefinitionImplementor: this.propertyToDependentPropertiesMap.get(name)) {
                    propertiesToBeInvalidated.addAll(flowPropertyDefinitionImplementor.getAllNames());
                    collectInvalidated(propertiesToBeInvalidated, flowPropertyDefinitionImplementor);
                }
            }
        }
    }

    public void monitorDependencies(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor) {
        for(FlowPropertyExpectation dependentOn: flowPropertyDefinitionImplementor.getPropertiesDependentOn()) {
            // HACK : should be looking at the dataClass as well. Chances are that if there are conflicts on the dataclass other problems will exist
            // and that check is too awkward for now.
            CUtilities.get(this.propertyToDependentPropertiesMap,dependentOn.getMapKey()).add(flowPropertyDefinitionImplementor);
        }
    }
}
