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

import org.amplafi.flow.FlowConfigurationException;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowPropertyDefinition;

import com.sworddance.beans.BeanWorker;

/**
 * SECURITY NOTE: Exposes internals to external api users unless we are careful
 * IMPORTANT SECURITY TODO: ( Could work iff we insisted that the property name be independent of the property path to get the value )
 *
 *
 * Uses reflection to trace to find the property value.
 * if at any point a null is returned then null is returned (no {@link NullPointerException} will be thrown)
 *
 * The root object can be either the flowPropertyProvider parameter in the {@link #get(FlowPropertyProvider, FlowPropertyDefinition)} call or another object
 * supplied in the constructor.
 *
 * TODO need to be able to set base as a String and that is the property name that will act as the base.
 * example: "messagePoint" means:
 * 1) retrieve "messagePoint"
 * 2) do reflection using the propertyName to get the value.
 *
 * TODO: Create ability to define the {@link FlowPropertyDefinition} Default property name would be the last property in the list. So "httpManager.cachedUris" would define
 * the "cachedUris" property (default)
 *
 * @author patmoore
 *
 */
public class ReflectionFlowPropertyValueProvider extends AbstractFlowPropertyValueProvider<FlowPropertyProvider> implements FlowPropertyValueProvider<FlowPropertyProvider> {

    private Object object;
    private String accessedAsName;
    private String startingPropertyName;
    private BeanWorker beanWorker;

    /**
     * SECURITY : should be mapped independently.
     * Use the {@link FlowPropertyProvider} that is passed in the {@link #get(FlowPropertyProvider, FlowPropertyDefinition)} as the starting object to trace for
     * using propertyName.
     *
     * @param propertyName
     */
    @Deprecated
    public ReflectionFlowPropertyValueProvider(String propertyName) {
        this(null, propertyName, propertyName);
    }
    public ReflectionFlowPropertyValueProvider(String accessedAsName, String mappedToProperty) {
        this(null, accessedAsName, mappedToProperty);
    }
    public ReflectionFlowPropertyValueProvider(Object object, String accessedAsName, String mappedToProperty) {
        super((Class<FlowPropertyProvider>)(object != null?FlowPropertyProvider.class: FlowPropertyProviderWithValues.class));
        this.object = object;
        if ( mappedToProperty.endsWith(".")) {
            throw new FlowConfigurationException("'.' as last character in ", mappedToProperty, "so no property to refer to.");
        } else if (mappedToProperty.startsWith(".")) {
            throw new FlowConfigurationException("'.' as first character in ", mappedToProperty, "so no object to dereference");
        }
        if ( this.object != null ) {
            this.beanWorker = new BeanWorker(mappedToProperty);
        } else {
            int indexFirst = mappedToProperty.indexOf(".");
            if (indexFirst == -1) {
                this.startingPropertyName = mappedToProperty;
            } else {
                this.startingPropertyName = mappedToProperty.substring(0, indexFirst);
                this.beanWorker = new BeanWorker(mappedToProperty.substring(indexFirst+1));
            }
        }
        this.accessedAsName = accessedAsName;
        super.addFlowPropertyDefinitionImplementators(new FlowPropertyDefinitionBuilder(accessedAsName));
    }

    /**
     * Used when an object other than the flowPropertyProvider passed in the {@link #get(FlowPropertyProvider, FlowPropertyDefinition)} should be used
     * as the root for tracing out properties.
     * @param object
     */
    public void setObject(Object object) {
        this.object = object;
    }
    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }
    /**
     *
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.flowproperty.FlowPropertyProvider, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        check(flowPropertyDefinition);
        if ( flowPropertyDefinition.isNamed(accessedAsName)) {
            T returned;
            if ( this.object != null) {
                returned = (T) this.beanWorker.getValue(this.object);
            } else {
                FlowPropertyProviderWithValues flowPropertyProviderWithValues = (FlowPropertyProviderWithValues) flowPropertyProvider;
                Object value = flowPropertyProviderWithValues.getProperty(this.startingPropertyName);
                if ( this.beanWorker == null) {
                    returned = (T) value;
                } else {
                    returned = (T) this.beanWorker.getValue(this.object);
                }
            }
            return returned;
        } else {
            throw fail(flowPropertyDefinition);
        }
    }
}
