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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.apache.commons.collections.CollectionUtils;
import com.sworddance.util.CUtilities;
import com.sworddance.util.map.ConcurrentInitializedMap;

import static com.sworddance.util.CUtilities.*;

/**
 * Listen for property changes will cause invalidation of other properties.
 * @author patmoore
 *
 */
public class InvalidatingFlowPropertyValueChangeListener implements FlowPropertyValueChangeListener {

    private ConcurrentMap<String, List<String>> propertyToDependentPropertiesMap = ConcurrentInitializedMap.<String, String>newConcurrentInitializedMapWithList();
    /**
     * @see org.amplafi.flow.flowproperty.FlowPropertyValueChangeListener#propertyChange(org.amplafi.flow.flowproperty.FlowPropertyProvider, java.lang.String, org.amplafi.flow.FlowPropertyDefinition, java.lang.String, java.lang.String)
     */
    @Override
    public String propertyChange(FlowPropertyProvider flowPropertyProvider, String namespace, FlowPropertyDefinition flowPropertyDefinition,
        String newValue, String oldValue) {
        if ( flowPropertyProvider instanceof FlowPropertyProviderWithValues) {
            Collection<String> propertiesNamesForProperty = CollectionUtils.intersection(flowPropertyDefinition.getAllNames(), this.propertyToDependentPropertiesMap.keySet());
            if ( isNotEmpty(propertiesNamesForProperty)) {
                Set<String> propertiesToBeInvalidated = new HashSet<String>();
                collectInvalidated(propertiesToBeInvalidated, propertiesNamesForProperty);
                for(String propertyToBeInvalidated: propertiesToBeInvalidated) {
                    // clear out the old values.
                    ((FlowPropertyProviderWithValues)flowPropertyProvider).setProperty(propertyToBeInvalidated, null);
                }
            }
        }
        return newValue;
    }

    /**
     * @param flowPropertyDefinition
     * @param propertiesToBeInvalidated
     */
    private void collectInvalidated(Set<String> propertiesToBeInvalidated, Collection<String>propertiesNamesForProperty) {

        if ( isNotEmpty(propertiesToBeInvalidated)) {
            // collect set of all properties affected.
            Set<String>additionalPropertiesToBePossiblyBeInvalidated = getInvalidateSet(propertiesToBeInvalidated);
            Collection<String>additionalPropertiesToBeInvalidated = CollectionUtils.subtract(additionalPropertiesToBePossiblyBeInvalidated, propertiesToBeInvalidated);
            if ( !additionalPropertiesToBeInvalidated.isEmpty()) {
                propertiesToBeInvalidated.addAll(additionalPropertiesToBeInvalidated);
                collectInvalidated(propertiesToBeInvalidated, additionalPropertiesToBeInvalidated);
            }
        }
    }

    /**
     * @param propertiesNamesForProperty
     * @return
     */
    private Set<String> getInvalidateSet(Collection<String> propertiesNamesForProperty) {
        Set<String> propertiesToBeInvalidated = new HashSet<String>();
        for(String propertyNameForProperty: propertiesNamesForProperty) {
            propertiesToBeInvalidated.addAll(this.propertyToDependentPropertiesMap.get(propertyNameForProperty));
        }
        return propertiesToBeInvalidated;
    }

    public void addDependency(FlowPropertyDefinitionImplementor flowPropertyDefinitionImplementor) {
        for(FlowPropertyExpectation dependentOn: flowPropertyDefinitionImplementor.getPropertiesDependentOn()) {
            CUtilities.get(this.propertyToDependentPropertiesMap,dependentOn).add(flowPropertyDefinitionImplementor.getName());
        }
    }
}
