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
import java.util.Set;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.apache.commons.collections.CollectionUtils;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationNullPointerException;

/**
 * @author patmoore
 * @param <FA>
 *
 */
public abstract class AbstractFlowPropertyValueProvider<FA extends FlowActivity> implements FlowPropertyValueProvider<FA> {
    private Set<String> propertiesHandled;
    /**
     * TODO: in future should define the property requirements?
     * TODO: also if some propertiesHandled may have different requirements. - so should be a Map<String,Set<String/ FlowPropertyDefinition>>
     */
    private Set<String> requiredProperties;

    protected AbstractFlowPropertyValueProvider(String...propertiesHandled) {
        this.propertiesHandled = new HashSet<String>();
        this.requiredProperties = new HashSet<String>();
        CollectionUtils.addAll(this.propertiesHandled, propertiesHandled);
    }

    protected void check(FlowPropertyDefinition flowPropertyDefinition) {
        if ( !isHandling(flowPropertyDefinition)) {
            throw new IllegalArgumentException(flowPropertyDefinition+": is not handled by "+this.getClass().getCanonicalName()+" only "+propertiesHandled);
        }
    }
    /**
     * avoids infinite loop by detecting when attempting to get the property that the FlowPropertyValueProvider is supposed to be supplying.
     * @param <T>
     * @param flowActivity
     * @param flowPropertyDefinition
     * @param propertyName
     * @return null if {@link FlowPropertyDefinition#isNamed(String)} is true otherwise the property retrieved.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getSafe(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
        if ( flowPropertyDefinition.isNamed(propertyName)) {
            return null; // TODO throw exception?
        } else {
            return (T) flowActivity.getProperty(propertyName);
        }
    }
    /**
     *
     * @param <T>
     * @param flowActivity
     * @param flowPropertyDefinition
     * @param propertyName
     * @return will not be null.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getRequired(FlowActivity flowActivity, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
        if ( flowPropertyDefinition.isNamed(propertyName)) {
            throw new ApplicationIllegalArgumentException(propertyName);
        } else {
            T result = (T) flowActivity.getProperty(propertyName);
            if ( result == null ) {
                throw new ApplicationNullPointerException(propertyName);
            }
            return result;
        }
    }

    public Collection<String> getPropertiesHandled() {
        return this.propertiesHandled;
    }

    /**
     *
     * @param flowPropertyDefinition
     * @return true if this {@link FlowPropertyDefinitionProvider} handles the {@link FlowPropertyDefinition}.
     */
    public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        for(String propertyName: propertiesHandled) {
            if (flowPropertyDefinition.isNamed(propertyName)) {
                return true;
            }
        }
        return false;
    }
    protected void addRequires(String...requiredProperties) {
        CollectionUtils.addAll(this.requiredProperties, requiredProperties);
    }
    public Collection<String> getRequiredProperties() {
        return this.requiredProperties;
    }
    /**
     * adds in the initFlowPropertyValueProvider(this) since I keep forgetting.
     * @param flowPropertyProvider
     * @param flowPropertyValueProvider
     * @param flowPropertyDefinitions
     */
    protected void addPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, FlowPropertyValueProvider<FA> flowPropertyValueProvider, FlowPropertyDefinitionImpl...flowPropertyDefinitions ) {
        for(FlowPropertyDefinitionImpl flowPropertyDefinition: flowPropertyDefinitions) {
            if ( !flowPropertyDefinition.isDefaultAvailable()) {
                flowPropertyDefinition.initFlowPropertyValueProvider(flowPropertyValueProvider);
            }
        }
        flowPropertyProvider.addPropertyDefinitions(flowPropertyDefinitions);
    }
}
