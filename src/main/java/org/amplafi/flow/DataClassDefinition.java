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

import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.translator.FlowTranslator;
import com.sworddance.beans.PropertyDefinition;

/**
 * Handles the issues around data structure for the {@link FlowPropertyDefinition}. This way
 * {@link FlowPropertyDefinition} focuses on required status, name, etc. and DataClassDefinition
 * focuses just on the way the data is structured. Allows the FlowDefinitionProperty structure to be
 * a middling complex chain of nested collections.
 *
 * @author patmoore
 */
public interface DataClassDefinition extends PropertyDefinition {

    DataClassDefinition getKeyDataClassDefinition();

    DataClassDefinition getElementDataClassDefinition();

    <T> String serialize(FlowPropertyDefinition flowPropertyDefinition, T value);

    <T, W> W serialize(FlowPropertyDefinition flowPropertyDefinition, W outputWriter, T value);

    <T> T deserialize(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition, Object value);

    /**
     * @return the flowTranslator
     */
    FlowTranslator getFlowTranslator();

    void setFlowTranslator(FlowTranslator flowTranslator);

    boolean isFlowTranslatorSet();

    /**
     * @return the element class (after unpeeling all the collection )
     */
    Class<?> getElementClass();

    /**
     * @return the collection class
     */
    Class<? extends Object> getCollection();

    /**
     * @return true if the data class has been explicitly defined.
     */
    boolean isDataClassDefined();

    /**
     * @param dataClass
     */
    void setDataClass(Class<? extends Object> dataClass);

    /**
     * @return the {@link Class} that this definition describes
     */
    Class<?> getDataClass();

    /**
     * @return true if this DataClassDefinition represents a collection of values.
     */
    boolean isCollection();

    /**
     * @param flowPropertyDefinition
     * @param value
     * @return true the string can be converted to the dataClass TODO: should really only need to be
     *         defined in the FlowTranslator.
     */
    boolean isDeserializable(FlowPropertyDefinition flowPropertyDefinition, Object value);

    /**
     * @return keyClass if this is a mapped collection.
     */
    Class<?> getKeyClass();

    @Override
    DataClassDefinition getElementPropertyDefinition();

    @Override
    DataClassDefinition getKeyPropertyDefinition();

    /**
     * @param dataClassDefinition
     */
    void merge(DataClassDefinition dataClassDefinition);

    /**
     * Checks to see that there is no conflict between the two DataClassDefinition
     *
     * @param dataClassDefinition
     * @return true if dataClassDefinition can be merged with this.
     */
    boolean isMergable(DataClassDefinition dataClassDefinition);

    /**
     * @param clazz
     * @return true if objects of 'clazz' can be stored in the structure defined by this
     *         DataClassDefinition. The attached {@link FlowTranslator} will be checked
     *         to see if conversion is possible.
     */
    boolean isAssignableFrom(Class<?> clazz);
}
