package org.amplafi.flow.impl;

import java.util.Optional;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueRequest;
import org.amplafi.flow.FlowTranslatorResolver;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;

public class FlowPropertyValueRequestImpl implements FlowPropertyValueRequest {

    private final FlowPropertyProvider flowPropertyProvider;

    private final FlowTranslatorResolver flowTranslatorResolver;

    private final Optional<String> propertyKey;

    private final Optional<Class<?>> propertyClass;

    private final Optional<?> rawSerializedObject;

    private final FlowPropertyDefinition flowPropertyDefinition;

    /**
     * @param flowPropertyProvider
     * @param flowTranslatorResolver
     * @param propertyKey
     * @param propertyClass
     * @param rawSerializedObject
     * @param flowPropertyDefinition
     */
    public FlowPropertyValueRequestImpl(FlowPropertyProvider flowPropertyProvider,
            FlowTranslatorResolver flowTranslatorResolver, Optional<String> propertyKey,
            Optional<Class<?>> propertyClass, Optional<?> rawSerializedObject,
            FlowPropertyDefinition flowPropertyDefinition) {
        this.flowPropertyProvider = flowPropertyProvider;
        this.flowTranslatorResolver = flowTranslatorResolver;
        this.propertyKey = propertyKey;
        this.propertyClass = propertyClass;
        this.rawSerializedObject = rawSerializedObject;
        this.flowPropertyDefinition = flowPropertyDefinition;
    }

    public FlowPropertyProvider getFlowPropertyProvider() {
        return flowPropertyProvider;
    }

    public FlowTranslatorResolver getFlowTranslatorResolver() {
        return flowTranslatorResolver;
    }

    public Optional<String> getPropertyKey() {
        return propertyKey;
    }

    public Optional<Class<?>> getPropertyClass() {
        return propertyClass;
    }

    public Optional<?> getRawSerializedObject() {
        return rawSerializedObject;
    }

    public FlowPropertyDefinition getFlowPropertyDefinition() {
        return flowPropertyDefinition;
    }
}
