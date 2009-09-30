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

import java.util.concurrent.Callable;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * @author patmoore
 * @param <FA>
 * @param <T>
 *
 */
public class FlowPropertyValueProviderCallableImpl<FA extends FlowActivity, T> implements FlowPropertyValueProvider<FA>, Callable<T> {

    private final FlowPropertyValueProvider<FA> flowPropertyValueProvider;
    private final FA flowActivity;
    private final FlowPropertyDefinition flowPropertyDefinition;
    public FlowPropertyValueProviderCallableImpl( FA flowActivity, FlowPropertyValueProvider<FA> flowPropertyValueProvider, FlowPropertyDefinition flowPropertyDefinition) {
        this.flowActivity = flowActivity;
        this.flowPropertyDefinition = flowPropertyDefinition;
        this.flowPropertyValueProvider = flowPropertyValueProvider;
    }
    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.FlowActivity, org.amplafi.flow.FlowPropertyDefinition)
     */
    @SuppressWarnings({ "hiding", "unchecked" })
    public <T> T get(FA flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        return (T) getFlowPropertyValueProvider().get(flowActivity, flowPropertyDefinition);
    }

    /**
     * @see java.util.concurrent.Callable#call()
     */
    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        return (T) get(getFlowActivity(), getFlowPropertyDefinition());
    }

    /**
     * @return the flowPropertyValueProvider
     */
    public FlowPropertyValueProvider<FA> getFlowPropertyValueProvider() {
        return flowPropertyValueProvider;
    }

    /**
     * @return the flowActivity
     */
    public FA getFlowActivity() {
        return flowActivity;
    }

    /**
     * @return the flowPropertyDefinition
     */
    public FlowPropertyDefinition getFlowPropertyDefinition() {
        return flowPropertyDefinition;
    }
}
