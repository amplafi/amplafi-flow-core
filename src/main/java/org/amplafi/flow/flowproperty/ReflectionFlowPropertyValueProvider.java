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

    private final Object object;
    /**
     * The property name. defaults to the string before the last '.'
     */
    private final String accessedAsName;
    /**
     * The string before the first '.'
     */
    private String startingPropertyName;
    private BeanWorker beanWorker;
    private BeanWorker beanWorkerIfPropertyFound;

    /**
     * @param object
     * @param accessedAsName
     * @param mappedToProperty if ends in '.' then accessedAsName is append as the final property to access.
     */
    public ReflectionFlowPropertyValueProvider(Object object, String accessedAsName, String mappedToProperty) {
        this(object, accessedAsName, mappedToProperty, new FlowPropertyDefinitionBuilder(accessedAsName));
    }
    /**
     * @param object
     * @param accessedAsName
     * @param mappedToProperty if ends in '.' then accessedAsName is append as the final property to access.
     * @param flowPropertyDefinitionBuilder for properties that are being built externally.
     */
    public ReflectionFlowPropertyValueProvider(Object object, String accessedAsName, String mappedToProperty, FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder) {
        super((Class<FlowPropertyProvider>)(object != null?FlowPropertyProvider.class: FlowPropertyProviderWithValues.class));
        this.object = object;
        this.accessedAsName = accessedAsName;
        if (mappedToProperty.startsWith(".")) {
            throw new FlowConfigurationException("'.' as first character in ", mappedToProperty, "so no object to dereference");
        }
        if ( mappedToProperty.endsWith(".")) {
            mappedToProperty += accessedAsName;
        }
        this.beanWorker = new BeanWorker(mappedToProperty);
        if ( this.object == null) {
            // may be getting value by dereferencing the FlowPropertyValueProvider supplied in the 'get' or start with a property
            // and then dereference the property
            int indexFirst = mappedToProperty.indexOf(".");
            if (indexFirst == -1) {
                this.startingPropertyName = mappedToProperty;
                this.beanWorkerIfPropertyFound = null;
            } else {
                this.startingPropertyName = mappedToProperty.substring(0, indexFirst);
                this.beanWorkerIfPropertyFound = new BeanWorker(mappedToProperty.substring(indexFirst+1));
            }
        } else {
            flowPropertyDefinitionBuilder.setDataClass(getPropertyType());
        }
        // Minor hack - relies on flowPropertyDefinitionBuilder not being replaced different flowPropertyDefinitionBuilder
        // while creating property with flowPropertyDefinitionBuilder
        // since this assumption is true ( 2013 Oct 31 ) this not a big deal.
        // needs flowPropertyDefinitionBuilder so that isHandling() can be answered correctly
        super.addFlowPropertyDefinitionImplementators(flowPropertyDefinitionBuilder);
    }

    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    public Class<?> getPropertyType() {
        if ( this.object != null ) {
            Class<?> dataClass = this.beanWorker.getPropertyType(this.object.getClass());
            return dataClass;
        } else {
            return null;
        }
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
                if ( value == null ) {
                    // no property: use full path starting with the flowPropertyProvider
                    returned = (T) this.beanWorker.getValue(flowPropertyProviderWithValues);
                } else if ( this.beanWorkerIfPropertyFound == null) {
                    // property found but no further derefencing is needed.
                    returned = (T) value;
                } else {
                    // got property but need more dereferencing.
                    returned = (T) this.beanWorkerIfPropertyFound.getValue(value);
                }
            }
            return returned;
        } else {
            throw fail(flowPropertyDefinition);
        }
    }
}
