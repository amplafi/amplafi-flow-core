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

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * @author patmoore
 * @param <FPP>
 * @param <T>
 *
 */
public class FlowPropertyValueProviderCallableImpl<FPP extends FlowPropertyProvider, T> implements FlowPropertyValueProvider<FPP>, Callable<T> {

    private final FlowPropertyValueProvider<FPP> flowPropertyValueProvider;
    private final FPP flowPropertyProvider;
    private final FlowPropertyDefinition flowPropertyDefinition;
    public FlowPropertyValueProviderCallableImpl( FPP flowPropertyProvider, FlowPropertyValueProvider<FPP> flowPropertyValueProvider, FlowPropertyDefinition flowPropertyDefinition) {
        this.flowPropertyProvider = flowPropertyProvider;
        this.flowPropertyDefinition = flowPropertyDefinition;
        this.flowPropertyValueProvider = flowPropertyValueProvider;
    }

    @SuppressWarnings({ "hiding", "unchecked" })
    public <T> T get(FPP flowActivity, FlowPropertyDefinition flowPropertyDefinition) {
        return (T) getFlowPropertyValueProvider().get(flowActivity, flowPropertyDefinition);
    }

    /**
     * @see java.util.concurrent.Callable#call()
     */
    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        return (T) get(flowPropertyProvider, getFlowPropertyDefinition());
    }

    /**
     * @return the flowPropertyValueProvider
     */
    public FlowPropertyValueProvider<FPP> getFlowPropertyValueProvider() {
        return flowPropertyValueProvider;
    }
    /**
     * @return the flowPropertyDefinition
     */
    public FlowPropertyDefinition getFlowPropertyDefinition() {
        return flowPropertyDefinition;
    }

    /**
     * @see org.amplafi.flow.FlowPropertyValueProvider#getFlowPropertyProviderClass()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<FPP> getFlowPropertyProviderClass() {
        return (Class<FPP>) flowPropertyProvider.getClass();
    }
	@Override
	public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
		return this.getFlowPropertyValueProvider().isHandling(flowPropertyExpectation);
	}
}
