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

import org.amplafi.flow.FlowPropertyExpectation;
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
public class ReflectionFlowPropertyValueProvider extends BeanWorker implements FlowPropertyValueProvider<FlowPropertyProvider> {

    private Object object;
    private String accessedAsName;

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
        super(mappedToProperty);
        this.accessedAsName = accessedAsName;
        this.object = object;
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
        final Object base = this.object==null?flowPropertyProvider:this.object;
        return (T) getValue(base, this.getPropertyName(0));
    }
    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#getFlowPropertyProviderClass()
     */
    @Override
    public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
        return FlowPropertyProvider.class;
    }
    @Override
    public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        if ( flowPropertyExpectation.isNamed(this.accessedAsName)) {
            return true;
        }
        // TODO SECURITY: remove following block of code
        for(String complexPropertyName:this.getPropertyNames()) {
            int beginIndex = complexPropertyName.lastIndexOf('.');
            String simplePropertyName = beginIndex < 0?complexPropertyName:complexPropertyName.substring(beginIndex+1);
            if ( flowPropertyExpectation.isNamed(simplePropertyName)) {
                return true;
            }
        }
        return false;
    }


}
