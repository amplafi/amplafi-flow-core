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

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * @author patmoore
 * @param <FPP>
 *
 */
public abstract class AbstractChainedFlowPropertyValueProvider<FPP extends FlowPropertyProvider> implements ChainedFlowPropertyValueProvider<FPP> {

    private Class<FPP> flowPropertyProviderClass;
    private FlowPropertyValueProvider<FPP> previous;

    protected AbstractChainedFlowPropertyValueProvider(Class<FPP> flowPropertyProviderClass) {
        this.flowPropertyProviderClass = flowPropertyProviderClass;
    }
    /**
     * @param flowPropertyProvider
     * @param flowPropertyDefinition
     * @return the value from the previous {@link org.amplafi.flow.FlowPropertyValueProvider} in the chain of {@link org.amplafi.flow.FlowPropertyValueProvider}
     */
    @SuppressWarnings("unchecked")
    protected <T> T getPropertyFromChain(FPP flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        T result = null;
        if ( getPrevious() != null ) {
            result = (T) getPrevious().get(flowPropertyProvider, flowPropertyDefinition);
        }
        return result;
    }

    /**
     * @see org.amplafi.flow.flowproperty.ChainedFlowPropertyValueProvider#setPrevious(org.amplafi.flow.FlowPropertyValueProvider)
     */
    @Override
    public void setPrevious(FlowPropertyValueProvider<FPP> previous) {
        this.previous = previous;
    }

    /**
     * @return the previous
     */
    protected FlowPropertyValueProvider<FPP> getPrevious() {
        return previous;
    }
    /**
     * @return the flowPropertyProviderClass
     */
    public Class<FPP> getFlowPropertyProviderClass() {
        return flowPropertyProviderClass;
    }


}
