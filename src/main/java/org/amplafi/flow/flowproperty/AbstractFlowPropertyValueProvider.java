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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowValueMapKey;
import org.amplafi.flow.FlowValuesMap;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationNullPointerException;

import static com.sworddance.util.CUtilities.*;

/**
 * @author patmoore
 * @param <FPP>
 *
 */
public abstract class AbstractFlowPropertyValueProvider<FPP extends FlowPropertyProvider> implements FlowPropertyValueProvider<FPP> {
    private final Class<FPP> flowPropertyProviderClass;
    @Deprecated
    private Set<String> propertiesHandled = new LinkedHashSet<String>();
    private List<FlowPropertyDefinitionImplementor> flowPropertyDefinitions;
    /**
     * TODO: in future should define the property requirements?
     * TODO: also if some propertiesHandled may have different requirements. - so should be a Map<String,Set<String/ FlowPropertyDefinition>>
     */
    private Set<String> requiredProperties = new LinkedHashSet<String>();
    private Set<String> optionalProperties = new LinkedHashSet<String>();

    protected AbstractFlowPropertyValueProvider() {
        this.flowPropertyProviderClass = initFlowPropertyProviderClass();
    }
    protected AbstractFlowPropertyValueProvider(Class<FPP>flowPropertyProviderClass) {
        if ( flowPropertyProviderClass == null) {
            this.flowPropertyProviderClass = initFlowPropertyProviderClass();
        } else {
            this.flowPropertyProviderClass = flowPropertyProviderClass;
        }
    }
    /**
     * {@link #getFlowPropertyProviderClass()} will return {@link FlowPropertyProviderWithValues} if FPP extends that class. otherwise {@link FlowPropertyProvider}
     * @param propertiesHandled first property listed is property returned by
     */
    protected AbstractFlowPropertyValueProvider(String...propertiesHandled) {
        this();
        this.propertiesHandled.addAll(Arrays.asList(propertiesHandled));
    }
    protected AbstractFlowPropertyValueProvider(Class<FPP>flowPropertyProviderClass, FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        this(flowPropertyProviderClass);
        this.flowPropertyDefinitions = new ArrayList<FlowPropertyDefinitionImplementor>();
        if ( isNotEmpty(flowPropertyDefinitions)) {
            this.flowPropertyDefinitions.addAll(Arrays.asList(flowPropertyDefinitions));
            for(FlowPropertyDefinition flowPropertyDefinition: flowPropertyDefinitions) {
                this.propertiesHandled.add(flowPropertyDefinition.getName());
            }
        }
    }

    protected AbstractFlowPropertyValueProvider(FlowPropertyDefinitionImplementor...flowPropertyDefinitions) {
        this((Class<FPP>)null, flowPropertyDefinitions);
    }
    /**
    *
    * @param propertiesHandled first property listed is property returned by
    */
   protected AbstractFlowPropertyValueProvider(Class<FPP>flowPropertyProviderClass, String...propertiesHandled) {
       this(flowPropertyProviderClass);
       this.propertiesHandled.addAll(Arrays.asList(propertiesHandled));
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
        for(String propertyName: propertiesHandled) {
            if (flowPropertyDefinition.isNamed(propertyName)) {
                return true;
            }
        }
        return false;
    }
    @SuppressWarnings("hiding")
    protected void addRequires(String...requiredProperties) {
        this.requiredProperties.addAll(Arrays.asList(requiredProperties));
    }
    public Collection<String> getRequiredProperties() {
        return this.requiredProperties;
    }
    @SuppressWarnings("hiding")
    protected void addOptional(String...optionalProperties) {
        this.optionalProperties.addAll(Arrays.asList(optionalProperties));
    }
    public Collection<String> getOptionalProperties() {
        return this.optionalProperties;
    }
    /**
     * adds in the initFlowPropertyValueProvider(this) since I keep forgetting.
     * @param flowPropertyProvider
     * @param flowPropertyDefinitions
     */
    @SuppressWarnings({"unchecked", "hiding"})
    protected void addPropertyDefinitions(FlowPropertyProviderImplementor flowPropertyProvider, FlowPropertyDefinitionImplementor...flowPropertyDefinitions ) {
        for(FlowPropertyDefinitionImplementor flowPropertyDefinition: flowPropertyDefinitions) {
            if ( !flowPropertyDefinition.isDefaultAvailable()) {
                flowPropertyDefinition.initFlowPropertyValueProvider(this);
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
        flowPropertyProvider.addPropertyDefinitions(flowPropertyDefinitions);
    }

    /**
     * @return the flowPropertyDefinitions
     */
    public List<FlowPropertyDefinitionImplementor> getFlowPropertyDefinitions() {
        return flowPropertyDefinitions;
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

    /**
     * @return the flowPropertyProviderClass
     */
    public Class<FPP> getFlowPropertyProviderClass() {
        return flowPropertyProviderClass;
    }
}
