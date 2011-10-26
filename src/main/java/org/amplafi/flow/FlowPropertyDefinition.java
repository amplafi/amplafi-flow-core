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
package org.amplafi.flow;

import java.util.List;
import java.util.Set;

import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.translator.FlowTranslator;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JsonSelfRenderer;

/**
 * Defines a property that will be assigned as part of a {@link Flow} or
 * {@link FlowActivity}. This allows the value to be available to the component
 * or page referenced by a {@link FlowActivity}.
 *
 * TODO: split into 2 interfaces so that there can be immutable FlowPropertyDefinition
 */
public interface FlowPropertyDefinition extends FlowPropertyExpectation, JsonSelfRenderer {

    /**
     * merge the information from source into this FlowPropertyDefinition.
     * @param source
     * @return true if merge was successful.
     */
    boolean merge(FlowPropertyDefinition source);

    /**
     * @return true if a default value for this property can be created.
     */
    boolean isAutoCreate();

    Object getDefaultObject(FlowPropertyProvider flowPropertyProvider);

    /**
     *
     * @param flowPropertyProvider
     * @return true if {@link #getDefaultObject(FlowPropertyProvider)} will return a value
     */
    boolean isDefaultObjectAvailable(FlowPropertyProvider flowPropertyProvider);
    // TODO: define circumstances where this is used as opposed to just not having a persister.
    boolean isCacheOnly();

    /**
     *
     * @return the {@link FlowTranslator} used to serialize/deserialize the property value.
     */
    FlowTranslator<?> getTranslator();

    boolean isMergeable(FlowPropertyDefinition source);
    boolean isDataClassMergeable(FlowPropertyDefinition flowPropertyDefinition);

    String getUiComponentParameterName();

    boolean isSaveBack();

    String getInitial();

    /**
     * @return all possible names for this FlowPropertyDefinition, including
     * {@link #getName()}.
     */
    Set<String> getAllNames();
    Set<String> getAlternates();

    String getValidators();

    boolean isAssignableFrom(Class<?> clazz);

    /**
     *
     * @param possiblePropertyName
     * @return true if {@link #getName()} or {@link #getAlternates()} equals possiblePropertyName ( case sensitive check)
     */
    boolean isNamed(String possiblePropertyName);

    /**
     * @return true if propertyScope has been explicitly set.
     */
    boolean isPropertyScopeSet();

    /**
     * @return true if propertyUsage has been explicitly set.
     */
    boolean isPropertyUsageSet();

    /**
     * @return true if there are {@link #getFlowPropertyValueChangeListeners()} ( in future may have change listeners that
     * are static. - i.e. don't ask to be updated. )
     */
    boolean isDynamic();

    /**
     * Needed for case when a FlowPropertyValueProvider supplies an object. Need to make sure the default object is saved in the FlowState, particularly if the flow transitions to another flow.
     * @return true if the value should be copied back to an exported flow values map.
     */
    boolean isCopyBackOnFlowSuccess();

    boolean isDefaultAvailable();

    boolean isExportable();

    /**
     * @return
     */
    List<Object> getObjectsNeedingToBeWired();
    
    <T> IJsonWriter serialize(IJsonWriter jsonWriter, T value);
    
}
