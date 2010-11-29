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
import java.util.Collections;
import java.util.List;

import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowValueMapKey;
import org.amplafi.flow.FlowValuesMap;

import static com.sworddance.util.CUtilities.*;

/**
 * support methods for {@link FlowPropertyDefinitionProvider} implementations.
 * Since this class does not implement any methods in {@link FlowPropertyDefinitionProvider} this class does not implement {@link FlowPropertyDefinitionProvider}.
 * @author patmoore
 *
 */
public abstract class AbstractFlowPropertyDefinitionProvider {
    private List<FlowPropertyDefinitionImplementor> flowPropertyDefinitions;
    /**
     * @param flowPropertyDefinitions
     */
    protected void setFlowPropertyDefinitions(FlowPropertyDefinitionImplementor... flowPropertyDefinitions) {
        this.flowPropertyDefinitions = new ArrayList<FlowPropertyDefinitionImplementor>();
        if ( isNotEmpty(flowPropertyDefinitions)) {
            Collections.addAll(this.flowPropertyDefinitions, flowPropertyDefinitions);
        }
    }
    /**
     * @return the flowPropertyDefinitions
     */
    public List<FlowPropertyDefinitionImplementor> getFlowPropertyDefinitions() {
        return flowPropertyDefinitions;
    }
    protected void addDefinedPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, List<? extends FlowPropertyExpectation> additionalConfiguration) {
    	if ( this.flowPropertyDefinitions != null) {
	        for(FlowPropertyDefinitionImplementor flowPropertyDefinition: flowPropertyDefinitions) {
	            initPropertyDefinition(flowPropertyProvider, flowPropertyDefinition);
	            flowPropertyProvider.addPropertyDefinitions(flowPropertyDefinition.clone());
	        }
	    }
    }
    /**
     * adds in the initFlowPropertyValueProvider(this) since I keep forgetting.
     * @param flowPropertyProvider
     * @param flowPropertyDefinitions
     */
    @SuppressWarnings({"hiding"})
    protected void addPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, FlowPropertyDefinitionImplementor...flowPropertyDefinitions ) {
        for(FlowPropertyDefinitionImplementor flowPropertyDefinition: flowPropertyDefinitions) {
            initPropertyDefinition(flowPropertyProvider, flowPropertyDefinition);
        }
        flowPropertyProvider.addPropertyDefinitions(flowPropertyDefinitions);
    }
    @SuppressWarnings({"unchecked"})
	protected void initPropertyDefinition(
			FlowPropertyProviderImplementor flowPropertyProvider,
			FlowPropertyDefinitionImplementor flowPropertyDefinition) {
		if ( !flowPropertyDefinition.isDefaultAvailable() && this instanceof FlowPropertyValueProvider) {
		    flowPropertyDefinition.initFlowPropertyValueProvider((FlowPropertyValueProvider)this);
		}
		// TODO : also create a "read-only" v. writeable property mechanism.
		if ( !flowPropertyDefinition.isCacheOnly() && flowPropertyDefinition.getFlowPropertyValuePersister() == null) {
		    FlowPropertyValuePersister<?> flowPropertyValuePersister = null;
		    if ( flowPropertyProvider instanceof FlowPropertyValuePersister) {
		        flowPropertyValuePersister = (FlowPropertyValuePersister<?>) flowPropertyProvider;
		    } else if ( this instanceof FlowPropertyValuePersister) {
		        flowPropertyValuePersister = (FlowPropertyValuePersister<?>) this;
		    }
		    flowPropertyDefinition.initFlowPropertyValuePersister(flowPropertyValuePersister);
		}
		if ( this instanceof FlowPropertyValueChangeListener ) {
		    flowPropertyDefinition.initFlowPropertyValueChangeListener((FlowPropertyValueChangeListener)this);
		}
	}
    protected <T extends CharSequence> T getAdditionalConfigParameter(FlowValuesMap<? extends FlowValueMapKey, ? extends CharSequence> additionalConfigurationParameters, Object key, T defaultValue) {
        T result;
        if ( additionalConfigurationParameters != null && additionalConfigurationParameters.containsKey(key)) {
            result = (T) additionalConfigurationParameters.get(key);
        } else {
            result = defaultValue;
        }
        return result;
    }
}
