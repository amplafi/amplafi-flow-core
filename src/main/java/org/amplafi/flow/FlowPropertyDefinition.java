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

import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.flowproperty.PropertyScope;
import org.amplafi.flow.translator.FlowTranslator;

/**
 * Defines a property that will be assigned as part of a {@link Flow} or
 * {@link FlowActivity}. This allows the value to be available to the component
 * or page referenced by a {@link FlowActivity}.
 *
 * {@link FlowPropertyDefinition} impose additional requirements on {@link FlowPropertyExpectation}'s requirements:
 * <ol>
 * <li>Must have a name</li>
 * <li>Must have a datatype</li>
 * <li>All other properties are explicitly set as well</li>
 * </ol>
 *
 * A Flow Property is, once defined, a typed java 'attribute' that is managed by the flow framework. It has a scope of validity
 * ({@link PropertyScope}), and other properties that might be altered by using a {@link FlowPropertyDefinitionBuilder} when you define
 * the property. Depending on where you're standing, you have various interfaces to fetch them using {@link FlowPropertyValueProvider}
 * or accessing - in a lower level - directly on a k/v map using getProperty, for example inside a {@link FlowActivity}
 *
 * A {@link FlowPropertyDefinition} is conceptual a {@link FlowPropertyExpectation} that has all attributes defined ( dataclass, name, et.al.)
 */
public interface FlowPropertyDefinition extends FlowPropertyExpectation {

    /**
     * merge the information from source into this FlowPropertyDefinition.
     * @param source
     * @return true if merge was successful.
     */
    @Deprecated // use FlowPropertyDefinitionBuilder
    boolean merge(FlowPropertyDefinition source);

    /**
     * @return true if a default value for this property can be created.
     */
    boolean isAutoCreate();

    Object getDefaultObject(FlowPropertyProvider flowPropertyProvider);

    /**
     * NOTE: an property could be cacheOnly AND !isReadOnly() this means the object must be saved before the current tx (request) completes.
     * @return true this means the object cannot be serialized.
     */
    boolean isCacheOnly();

    /**
     * A {@link org.amplafi.flow.flowproperty.FlowPropertyDefinitionProvider} may provide a {@link org.amplafi.flow.flowproperty.FlowPropertyValuePersister}
     * but the created {@link FlowPropertyDefinition} may often times be used in situations where the changes to the property should not be
     * persisted.
     *
     * Usually such changes to a readonly property are a sign of a bug or hack attempt.
     *
     * TODO: log property changes that are not persisted.
     * @return true if property changes are not to be persisted.
     */
    boolean isReadOnly();

    /**
     *
     * @return the {@link FlowTranslator} used to serialize/deserialize the property value.
     */
    FlowTranslator<?> getTranslator();

    boolean isMergeable(FlowPropertyDefinition source);
    boolean isDataClassMergeable(FlowPropertyDefinition flowPropertyDefinition);

    /**
     * HACK : we must not require manual checking of saveBack. Property should know if it should be copied back to the flow values map
     * @return true if the property should be serialize into the FlowMap before being saved
     */
    @Deprecated
    boolean isSaveBack();

    /**
     * @return all possible names for this FlowPropertyDefinition, including
     * {@link #getName()}.
     */
    Set<String> getAllNames();

    boolean isFlowTranslatorSet();

    boolean isNamed(Class<?> byClassName);

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
     * This is the cumulative set of objects that need to be wired with services.
     * @return list of objects that need to be wired
     */
    List<Object> getObjectsNeedingToBeWired();

    <T, W> W serialize(W outputWriter, T value);

    <T> String serialize(T object);
    <V> V deserialize(FlowPropertyProvider flowPropertyProvider, Object serializedObject) throws FlowException;

}
