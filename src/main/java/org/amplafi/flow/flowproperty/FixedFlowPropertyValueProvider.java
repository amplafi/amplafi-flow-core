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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyExpectation;
import org.amplafi.flow.FlowPropertyValueProvider;

import com.sworddance.util.ApplicationIllegalArgumentException;
import com.sworddance.util.ApplicationIllegalStateException;

import org.apache.commons.lang.ObjectUtils;

/**
 * {@link org.amplafi.flow.FlowPropertyValueProvider} that handles statically provided values.
 *
 */
public class FixedFlowPropertyValueProvider implements FlowPropertyValueProvider<FlowPropertyProvider> {

    private final Object defaultObject;
    private final Class<?> defaultClass;
    /**
     * Used to tell difference between null as the value and null as not initialized.
     * (This is not visible to outside code)
     */
    private static Object NULL = new Object() {
        @Override
        public String toString() {
            return "<null>";
        }
    };

    /**
     * Used when want to force a {@link FlowPropertyDefinition} to not have the default {@link FlowPropertyValueProvider} assignments happen.
     */
    public static FixedFlowPropertyValueProvider NULL_INSTANCE = new FixedFlowPropertyValueProvider(NULL);
    /**
     * key off of FlowPropertyDefinition so that different FPD can translate defaultObject differently
     */
    private ConcurrentMap<FlowPropertyDefinition, Object> map = new ConcurrentHashMap<FlowPropertyDefinition, Object>();

    public FixedFlowPropertyValueProvider(Object defaultObject) {
        ApplicationIllegalArgumentException.notNull(defaultObject, "Fixed value cannot be null. If explicit null is really intended use FixedFlowPropertyValueProvider.NULL_INSTANCE");
        this.defaultObject = defaultObject;
        this.defaultClass = defaultObject.getClass();
    }
    public FixedFlowPropertyValueProvider(Class<?> defaultClass) {
        ApplicationIllegalArgumentException.notNull(defaultClass, "Fixed defaultClass cannot be null.");
        ApplicationIllegalArgumentException.valid(!defaultClass.isAnnotation() && !defaultClass.isInterface() && !defaultClass.isPrimitive(), defaultClass, ": cannot be instantiated.");
        this.defaultObject = null;
        this.defaultClass = defaultClass;
    }
    /**
     * @param flowPropertyDefinition
     */
    public void convertable(FlowPropertyDefinition flowPropertyDefinition) {
        if ( flowPropertyDefinition.getDataClassDefinition().isDataClassDefined()) {
            DataClassDefinition dataClassDefinition = flowPropertyDefinition.getDataClassDefinition();
            ApplicationIllegalStateException.checkState( dataClassDefinition.isDeserializable(flowPropertyDefinition, this.getDefaultObject()),
                this, " cannot convert value=", this.getDefaultObject());
        }
    }

    @Override
    @SuppressWarnings({  "unchecked" })
    public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        T value;
        if ( this.defaultObject == null) {
            // no default object but default class.

        }
        if ( !map.containsKey(flowPropertyDefinition) ) {
            convertable(flowPropertyDefinition);
            value = (T) flowPropertyDefinition.getDataClassDefinition().deserialize(flowPropertyProvider, flowPropertyDefinition, this.getDefaultObject());
            if ( value == null ) {
                map.putIfAbsent(flowPropertyDefinition, NULL);
            } else {
                map.putIfAbsent(flowPropertyDefinition, value);
            }
        }
        value = (T) map.get(flowPropertyDefinition);
        if ( value == NULL) {
            value = null;
        }
        return value;
    }
    /**
     * @return the value as a string.
     */
    public String getDefaultString() {
        return ObjectUtils.toString(getDefaultObject());
    }

    public Class<?> getSuggestedClass() {
        if ( this.defaultClass == null ) {
            return String.class;
        } else {
            return this.defaultClass;
        }
    }

    @Override
    public int hashCode() {
        return this.getDefaultObject() == null?1:this.getDefaultObject().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if ( o instanceof FixedFlowPropertyValueProvider) {
            return ObjectUtils.equals(this.getDefaultObject(), ((FixedFlowPropertyValueProvider)o).getDefaultObject());
        } else {
            return ObjectUtils.equals(this.getDefaultObject(), o);
        }
    }
    /**
     * @return the defaultObject
     */
    public Object getDefaultObject() {
        return defaultObject == NULL?null:this.defaultObject;
    }
    @Override
    public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
        return FlowPropertyProvider.class;
    }
    @Override
    public boolean isHandling(FlowPropertyExpectation flowPropertyExpectation) {
        return this.getDefaultObject() == null || flowPropertyExpectation.isAssignableFrom(this.getSuggestedClass());
    }
    @Override
    public String toString() {
        return "FixedProperty value="+getDefaultString();
    }
    /**
     * @param <FPP>
     * @param defaultObject
     * @param flowPropertyDefinition
     * @param testAndConfigFlowPropertyDefinition if true then set the flowPropertyDefinition based on defaultObject.getClass()
     * @return the created fixedFlowPropertyValueProvider
     */
    public static <FPP extends FlowPropertyProvider> FixedFlowPropertyValueProvider newFixedFlowPropertyValueProvider(Object defaultObject,
        FlowPropertyDefinitionImplementor flowPropertyDefinition, boolean testAndConfigFlowPropertyDefinition) {
        FixedFlowPropertyValueProvider fixedFlowPropertyValueProvider = null;
        if ( defaultObject != null ) {
            fixedFlowPropertyValueProvider = new FixedFlowPropertyValueProvider(defaultObject);
            fixedFlowPropertyValueProvider.convertable(flowPropertyDefinition);
            if ( testAndConfigFlowPropertyDefinition ) {
                DataClassDefinition dataClassDefinition = flowPropertyDefinition.getDataClassDefinition();
                if (dataClassDefinition.isDataClassDefined()) {
                    if (!dataClassDefinition.getDataClass().isPrimitive()) {
                        // really need to handle the autobox issue better.
                        dataClassDefinition.getDataClass().cast(defaultObject);
                    }
                } else {
                    dataClassDefinition.setDataClass(defaultObject.getClass());
                }
            }
        } else {
            fixedFlowPropertyValueProvider = NULL_INSTANCE;
        }
        return fixedFlowPropertyValueProvider;
    }
    /**
     * EXPERIMENTAL:
     *
     * Sort of duplicates the default object from a FlowTranslator...
     * but could make the FlowPropertyDefinitionImpl code cleaner because we can get rid of isAutoCreate() and isDefauftClassAvailable() concepts.
     *
     * @param <FPP>
     * @param flowPropertyDefinition
     * @param testAndConfigFlowPropertyDefinition
     * @return the created fixedFlowPropertyValueProvider
     */
    public static <FPP extends FlowPropertyProvider> FixedFlowPropertyValueProvider newFixedFlowPropertyValueProvider(
        FlowPropertyDefinitionImplementor flowPropertyDefinition, boolean testAndConfigFlowPropertyDefinition) {

        return newFixedFlowPropertyValueProvider(flowPropertyDefinition.getDataClassDefinition().getPropertyClass(), flowPropertyDefinition, testAndConfigFlowPropertyDefinition);
    }
    @SuppressWarnings("unchecked")
    public static <FPP extends FlowPropertyProvider> FixedFlowPropertyValueProvider newFixedFlowPropertyValueProvider(Class<?> defaultClass,
        FlowPropertyDefinitionImplementor flowPropertyDefinition, boolean testAndConfigFlowPropertyDefinition) {
        FixedFlowPropertyValueProvider fixedFlowPropertyValueProvider = null;

        ApplicationIllegalArgumentException.valid(!defaultClass.isAnnotation(), defaultClass, "is an annotation. Annotations cannot be instatiated.");
        if ( defaultClass == int.class) {
            return newFixedFlowPropertyValueProvider(Integer.valueOf(0), flowPropertyDefinition, testAndConfigFlowPropertyDefinition);
        } else if ( defaultClass == long.class) {
            return newFixedFlowPropertyValueProvider(Long.valueOf(0), flowPropertyDefinition, testAndConfigFlowPropertyDefinition);
        } else if ( defaultClass == boolean.class) {
            return newFixedFlowPropertyValueProvider(Boolean.valueOf(false), flowPropertyDefinition, testAndConfigFlowPropertyDefinition);
        } else if ( defaultClass.isEnum()) {
            // use the first enum as the default.
            return newFixedFlowPropertyValueProvider(((Class<Enum<?>>)defaultClass).getEnumConstants()[0], flowPropertyDefinition, testAndConfigFlowPropertyDefinition);
        }
        Class<?> classToCreate;
        if ( defaultClass == Set.class) {
            classToCreate = LinkedHashSet.class;
        } else if ( defaultClass == List.class) {
            classToCreate = ArrayList.class;
        } else if ( defaultClass == Map.class) {
            classToCreate = HashMap.class;
        } else {
            classToCreate = defaultClass;
        }
        fixedFlowPropertyValueProvider = new FixedFlowPropertyValueProvider(classToCreate);

        fixedFlowPropertyValueProvider.convertable(flowPropertyDefinition);
        if ( testAndConfigFlowPropertyDefinition ) {
            DataClassDefinition dataClassDefinition = flowPropertyDefinition.getDataClassDefinition();
            if (dataClassDefinition.isDataClassDefined()) {
                ApplicationIllegalArgumentException.valid(dataClassDefinition.isAssignableFrom(defaultClass), dataClassDefinition," cannot be assigned ");
            } else {
                dataClassDefinition.setDataClass(defaultClass);
            }
        }
        return fixedFlowPropertyValueProvider;
    }
}
