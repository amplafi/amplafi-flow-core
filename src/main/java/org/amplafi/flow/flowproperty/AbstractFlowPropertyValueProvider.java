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
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;
import com.sworddance.util.ApplicationIllegalArgumentException;

/**
 * @author patmoore
 * @param <FPP>
 *
 */
public abstract class AbstractFlowPropertyValueProvider<FPP extends FlowPropertyProvider> extends AbstractFlowPropertyDefinitionProvider implements FlowPropertyValueProvider<FPP> {
    private final Class<FPP> flowPropertyProviderClass;

    protected AbstractFlowPropertyValueProvider(Class<FPP>flowPropertyProviderClass, FlowPropertyDefinitionBuilder...flowPropertyDefinitionBuilders) {
        super(flowPropertyDefinitionBuilders);
        if ( flowPropertyProviderClass == null) {
            this.flowPropertyProviderClass = initFlowPropertyProviderClass();
        } else {
            this.flowPropertyProviderClass = flowPropertyProviderClass;
        }
    }
    protected AbstractFlowPropertyValueProvider(Class<FPP>flowPropertyProviderClass, FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        super(flowPropertyDefinitions);
        if ( flowPropertyProviderClass == null) {
            this.flowPropertyProviderClass = initFlowPropertyProviderClass();
        } else {
            this.flowPropertyProviderClass = flowPropertyProviderClass;
        }
    }

    protected AbstractFlowPropertyValueProvider(FlowPropertyDefinitionBuilder...flowPropertyDefinitionBuilders) {
        super(flowPropertyDefinitionBuilders);
        this.flowPropertyProviderClass = initFlowPropertyProviderClass();
    }

    protected AbstractFlowPropertyValueProvider(FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        super(flowPropertyDefinitions);
        this.flowPropertyProviderClass = initFlowPropertyProviderClass();
    }
    @Override
    public FlowPropertyDefinitionBuilder getFlowPropertyDefinitionBuilder(String propertyName, Class<?> dataClass) {
        FlowPropertyDefinitionBuilder flowPropertyDefinitionBuilder = super.getFlowPropertyDefinitionBuilder(propertyName, dataClass);
        if (flowPropertyDefinitionBuilder != null) {
            flowPropertyDefinitionBuilder.initFlowPropertyValueProvider(this);
        }
        return flowPropertyDefinitionBuilder;
    }
    @SuppressWarnings("unchecked")
    private Class<FPP> initFlowPropertyProviderClass() {
        Class<FPP> clazz;
        // TODO: is there a way to find out the class of FPP - last I checked there wasn't
        try {
            clazz = (Class<FPP>) FlowPropertyProviderWithValues.class;
        } catch (ClassCastException e) {
            clazz = (Class<FPP>) FlowPropertyProvider.class;
        }
        return clazz;
    }

    protected void check(FlowPropertyDefinition flowPropertyDefinition) {
       ApplicationIllegalArgumentException.valid(isHandling(flowPropertyDefinition),
           flowPropertyDefinition,": is not handled by ",this.getClass().getCanonicalName()," only ",getFlowPropertyDefinitionNames());
    }
    /**
     * used after a chain of isHandling() checks
     * @param flowPropertyDefinition
     */
    protected ApplicationIllegalArgumentException fail(FlowPropertyDefinition flowPropertyDefinition) {
        return ApplicationIllegalArgumentException.fail("Hard coded fail: ", isHandling(flowPropertyDefinition),
           flowPropertyDefinition,": is not handled by ",this.getClass().getCanonicalName()," only ",getFlowPropertyDefinitionNames());
    }
    /**
     * avoids infinite loop by detecting when attempting to get the property that the FlowPropertyValueProvider is supposed to be supplying.
     *
     * ONLY use when in the get() method not when doing a saveChanges() call.
     *
     * Use {@link #getRequired(FlowPropertyProviderWithValues, FlowPropertyDefinition, String, Object...)} if a value should always be returned.
     * @param <T>
     * @param flowPropertyProvider -- should this be FPP?
     * @param flowPropertyDefinition
     * @param propertyName
     * @return null if {@link FlowPropertyDefinition#isNamed(String)} is true otherwise the property retrieved.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getSafe(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName) {
        return (T) getSafe(flowPropertyProvider, flowPropertyDefinition, propertyName, null);
    }
    protected <T> T getSafe(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, Class<T> propertyClass) {
        return getSafe(flowPropertyProvider, flowPropertyDefinition, null, propertyClass);
    }
    protected <T> T getSafe(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName, Class<? extends T> propertyClass) {
        if ( propertyName != null ) {
            if (!flowPropertyDefinition.isNamed(propertyName) ) {
                return flowPropertyProvider.getProperty(propertyName, propertyClass);
            }
        } else if ( propertyClass != null ) {
            if ( !flowPropertyDefinition.isNamed(propertyClass) ) {
                return flowPropertyProvider.getProperty(propertyClass);
            }
        }
        // TODO throw exception?
        return null;
    }
    public Collection<String> getPropertiesHandled() {
        return this.getFlowPropertyDefinitionNames();
    }

    /**
     *
     * @param flowPropertyExpectation
     * @return true if this {@link FlowPropertyDefinitionProvider} handles the {@link FlowPropertyDefinition}.
     */
    @Override
    public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        for(String propertyName: getPropertiesHandled()) {
            if (flowPropertyExpectation.isNamed(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the flowPropertyProviderClass
     */
    @Override
    public Class<FPP> getFlowPropertyProviderClass() {
        return this.flowPropertyProviderClass;
    }
}
