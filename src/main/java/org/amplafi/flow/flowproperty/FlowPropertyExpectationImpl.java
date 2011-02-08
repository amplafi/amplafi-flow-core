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

import java.util.Arrays;
import java.util.List;

import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * Used to help configure properties created by {@link FlowPropertyDefinitionProvider}s
 * @author patmoore
 *
 */
public class FlowPropertyExpectationImpl implements FlowPropertyExpectation {

    private final List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners;

    private final String name;
    private final FlowActivityPhase propertyRequired;
    private final PropertyScope propertyScope;
    private final PropertyUsage propertyUsage;

    private final FlowPropertyValueProvider<?> flowPropertyValueProvider;
    private final FlowPropertyValuePersister flowPropertyValuePersister;

    private final PropertySecurity propertySecurity;

    /**
     * All properties should have the
     * @param flowActivityPhase
     */
    public FlowPropertyExpectationImpl(FlowActivityPhase flowActivityPhase) {
        this(null, flowActivityPhase, null, null, null, null, null, null);
    }
    public FlowPropertyExpectationImpl(String name, FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        this(name, null, null, null, null, null, null, Arrays.asList(flowPropertyValueChangeListener));
    }
    public FlowPropertyExpectationImpl(String name, FlowPropertyValueProvider flowPropertyValueProvider) {
        this(name, null, null, null, null, flowPropertyValueProvider, null, null);
    }
    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
            PropertySecurity propertySecurity, FlowPropertyValueChangeListener flowPropertyValueChangeListener) {
        this(name, propertyRequired, propertyScope, propertyUsage, propertySecurity, null, null, Arrays.asList(flowPropertyValueChangeListener));
    }

    public FlowPropertyExpectationImpl(String name, FlowActivityPhase propertyRequired, PropertyScope propertyScope, PropertyUsage propertyUsage,
            PropertySecurity propertySecurity,
            FlowPropertyValueProvider<? extends FlowPropertyProvider> flowPropertyValueProvider, FlowPropertyValuePersister flowPropertyValuePersister, List<FlowPropertyValueChangeListener> flowPropertyValueChangeListeners) {
        this.name = name;
        this.propertyRequired = propertyRequired;
        this.propertyScope = propertyScope;
        this.propertyUsage = propertyUsage;
        this.propertySecurity = propertySecurity;
        this.flowPropertyValueChangeListeners = flowPropertyValueChangeListeners;
        this.flowPropertyValueProvider = flowPropertyValueProvider;
        this.flowPropertyValuePersister = flowPropertyValuePersister;
    }
    /**
     * @return the flowPropertyValueChangeListeners
     */
    public List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners() {
        return flowPropertyValueChangeListeners;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String getMapKey() {
        return getName();
    }
    /**
     * @return the propertyRequired
     */
    public FlowActivityPhase getPropertyRequired() {
        return propertyRequired;
    }
    /**
     * @return the propertyScope
     */
    public PropertyScope getPropertyScope() {
        return propertyScope;
    }
    /**
     * @return the propertyUsage
     */
    public PropertyUsage getPropertyUsage() {
        return propertyUsage;
    }
    @SuppressWarnings("unchecked")
    public <FA extends FlowPropertyProvider> FlowPropertyValueProvider<FA> getFlowPropertyValueProvider() {
        return (FlowPropertyValueProvider<FA>)flowPropertyValueProvider;
    }

    @Override
    public boolean isApplicable(FlowPropertyDefinitionImplementor flowPropertyDefinition) {
        return getName() == null || flowPropertyDefinition.isNamed(getName());
    }
    public FlowPropertyValuePersister getFlowPropertyValuePersister() {
        return flowPropertyValuePersister;
    }
    public PropertySecurity getPropertySecurity() {
        return propertySecurity;
    }

    public static FlowPropertyExpectationImpl createDefaultExpectation(String name, Object defaultObject) {
    	return new FlowPropertyExpectationImpl(name, new FixedFlowPropertyValueProvider<FlowPropertyProvider>(defaultObject));
    }
}
