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
package org.amplafi.flow;

import java.util.List;
import java.util.Set;

import org.amplafi.flow.flowproperty.PropertyScope;

/**
 * Defines a property that will be assigned as part of a {@link Flow} or
 * {@link FlowActivity}. This allows the value to be available to the component
 * or page referenced by a {@link FlowActivity}.
 */
public interface FlowPropertyDefinition {
    String getName();

    <T> String serialize(T object);

    FlowPropertyDefinition initialize();

    DataClassDefinition getDataClassDefinition();

    boolean merge(FlowPropertyDefinition source);

    /**
     * @return the {@link Class} of this property
     */
    Class<?> getDataClass();

    <V> V parse(String value) throws FlowException;

    /**
     * @return true if a default value for this property can be created.
     */
    boolean isAutoCreate();

    Object getDefaultObject(FlowActivity flowActivity);

    boolean isCacheOnly();

    <FA extends FlowActivity> void setFlowPropertyValueProvider(FlowPropertyValueProvider<FA> flowPropertyValueProvider);

    <FA extends FlowActivity> FlowPropertyValueProvider<FA> getFlowPropertyValueProvider();

    FlowPropertyDefinition clone();

    /**
     * @return if property is local to the flow activity
     * @see org.amplafi.flow.PropertyUsage#activityLocal
     */
    boolean isLocal();

    void setRequired(boolean required);

    boolean isMergeable(FlowPropertyDefinition source);

    String getUiComponentParameterName();

    PropertyRequired getPropertyRequired();
    void setPropertyRequired(PropertyRequired propertyRequired);

    boolean isSaveBack();

    String getInitial();

//    /**
//     * @return if true, the initial value of the property can be overridden by a
//     * passed in value.
//     */
//    boolean isInitialValueOptional();

    /**
     * @return all possible names for this FlowPropertyDefinition, including
     * {@link #getName()}.
     */
    Set<String> getAllNames();
    Set<String> getAlternates();

    /**
     * @return how to use the property.
     */
    PropertyUsage getPropertyUsage();

    void setPropertyUsage(PropertyUsage propertyUsage);

    String getNamespaceKey(FlowState flowState, FlowActivity flowActivity);

    /**
     * the list of namespaces used to find the property value in the FlowState map.
     * This list is constructed by examining the PropertyUsage constraints.
     * @param flowState (may be null )
     * @param flowActivity (may be null )
     * @return ordered collection used to find/set this property.
     */
    List<String> getNamespaceKeySearchList(FlowState flowState, FlowActivity flowActivity);

    String getValidators();

    boolean isAssignableFrom(Class<?> clazz);

    boolean isRequired();
    /**
     *
     * @param possiblePropertyName
     * @return true if {@link #getName()} or {@link #getAlternates()} equals possiblePropertyName ( case sensitive check)
     */
    boolean isNamed(String possiblePropertyName);

    /**
     * @param activitylocal
     */
    void setPropertyScope(PropertyScope activitylocal);
    PropertyScope getPropertyScope();

    /**
     * @return
     */
    boolean isPropertyScopeSet();

    /**
     * @return
     */
    boolean isPropertyUsageSet();
}
