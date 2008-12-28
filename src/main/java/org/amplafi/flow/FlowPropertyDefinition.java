package org.amplafi.flow;

import org.amplafi.flow.FlowPropertyValueProvider;

import java.util.Set;

/**
 * @author Andreas Andreou
 */
public interface FlowPropertyDefinition {
    String getName();

    <T> String serialize(T object);

    FlowPropertyDefinition initialize();

    DataClassDefinition getDataClassDefinition();

    boolean merge(FlowPropertyDefinition source);

    Class<? extends Object> getDataClass();

    @SuppressWarnings("unchecked")
    <V> V parse(String value) throws FlowException;

    boolean isAutoCreate();

    Object getDefaultObject(FlowActivity flowActivity);

    boolean isCacheOnly();

    void setFlowPropertyValueProvider(FlowPropertyValueProvider flowPropertyValueProvider);

    FlowPropertyValueProvider getFlowPropertyValueProvider();

    FlowPropertyDefinition clone();

    boolean isLocal();

    void setRequired(boolean required);

    boolean isMergeable(FlowPropertyDefinition source);

    String getParameterName();

    PropertyRequired getPropertyRequired();
    void setPropertyRequired(PropertyRequired propertyRequired);

    boolean isSaveBack();

    String getInitial();

    boolean isInitialMode();

    Set<String> getAlternates();

    PropertyUsage getPropertyUsage();

    void setPropertyUsage(PropertyUsage propertyUsage);

    String getValidators();

    boolean isAssignableFrom(Class<?> clazz);

    boolean isRequired();
}
