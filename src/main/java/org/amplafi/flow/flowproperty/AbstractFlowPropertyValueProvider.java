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
import java.util.LinkedHashSet;
import java.util.Set;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;
import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationNullPointerException;

import static com.sworddance.util.CUtilities.*;

/**
 * @author patmoore
 * @param <FPP>
 *
 */
public abstract class AbstractFlowPropertyValueProvider<FPP extends FlowPropertyProvider> extends AbstractFlowPropertyDefinitionProvider implements FlowPropertyValueProvider<FPP> {
    private final Class<FPP> flowPropertyProviderClass;
    @Deprecated
    private Set<String> propertiesHandled = new LinkedHashSet<String>();


    protected AbstractFlowPropertyValueProvider() {
        this.flowPropertyProviderClass = initFlowPropertyProviderClass();
    }
    protected AbstractFlowPropertyValueProvider(Class<FPP>flowPropertyProviderClass, FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        super(flowPropertyDefinitions);
        if ( flowPropertyProviderClass == null) {
            this.flowPropertyProviderClass = initFlowPropertyProviderClass();
        } else {
            this.flowPropertyProviderClass = flowPropertyProviderClass;
        }
        if ( isNotEmpty(flowPropertyDefinitions)) {
            for(FlowPropertyDefinition flowPropertyDefinition: flowPropertyDefinitions) {
                this.propertiesHandled.add(flowPropertyDefinition.getName());
            }
        }
    }

    protected AbstractFlowPropertyValueProvider(FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        this((Class<FPP>)null, flowPropertyDefinitions);
    }
   /**
    * @return
    *
    */
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
            flowPropertyDefinition,": is not handled by ",this.getClass().getCanonicalName()," only ",propertiesHandled);
    }
    /**
     * avoids infinite loop by detecting when attempting to get the property that the FlowPropertyValueProvider is supposed to be supplying.
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
    protected <T> T getSafe(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName, Class<? extends T> expected) {
        if ( flowPropertyDefinition.isNamed(propertyName)) {
            return null;
        } else {
            return flowPropertyProvider.getProperty(propertyName, expected);
        }
    }
    /**
     *
     * @param <T>
     * @param flowPropertyProvider -- should this be FPP?
     * @param flowPropertyDefinition
     * @param propertyName
     * @return will not be null.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getRequired(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName, Object...messages) {
        return (T) this.getRequired(flowPropertyProvider, flowPropertyDefinition, propertyName, null, messages);
    }
    protected <T> T getRequired(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, String propertyName, Class<? extends T> expected, Object...messages) {
        ApplicationIllegalArgumentException.valid(!flowPropertyDefinition.isNamed(propertyName), propertyName);
        T result = flowPropertyProvider.getProperty(propertyName, expected);
        ApplicationNullPointerException.notNull(result, propertyName, messages);
        return result;
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
        for(String propertyName: getPropertiesHandled()) {
            if (flowPropertyDefinition.isNamed(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the flowPropertyProviderClass
     */
    public Class<FPP> getFlowPropertyProviderClass() {
        return flowPropertyProviderClass;
    }
}
