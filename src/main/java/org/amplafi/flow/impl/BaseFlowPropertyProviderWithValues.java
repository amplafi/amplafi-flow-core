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
package org.amplafi.flow.impl;

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;

/**
 * @author patmoore
 *
 */
public abstract class BaseFlowPropertyProviderWithValues<FPP extends FlowPropertyProviderWithValues> extends BaseFlowPropertyProvider<FPP> implements FlowPropertyProviderWithValues {

    /**
     *
     */
    public BaseFlowPropertyProviderWithValues() {
    }

    /**
     * @param definition
     */
    public BaseFlowPropertyProviderWithValues(FPP definition) {
        super(definition);
    }

    /**
     * @param flowPropertyProviderName
     */
    public BaseFlowPropertyProviderWithValues(String flowPropertyProviderName) {
        super(flowPropertyProviderName);
    }

    /**
     * @see org.amplafi.flow.FlowActivity#getProperty(java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getProperty(String key) {
        return (T) this.getProperty(key, null);
    }
    public final <T> T getProperty(Class<? extends T> clazz) {
        String key = FlowPropertyDefinitionBuilder.toPropertyName(clazz);
        return this.getProperty(key, clazz);
    }
}
