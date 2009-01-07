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

    <V> V parse(String value) throws FlowException;

    /**
     * @return true if a default value for this property can be created.
     */
    boolean isAutoCreate();

    Object getDefaultObject(FlowActivity flowActivity);

    boolean isCacheOnly();

    void setFlowPropertyValueProvider(FlowPropertyValueProvider flowPropertyValueProvider);

    FlowPropertyValueProvider getFlowPropertyValueProvider();

    FlowPropertyDefinition clone();

    /**
     * @return if property is local to the flow activity
     * @see org.amplafi.flow.PropertyUsage#activityLocal
     */
    boolean isLocal();

    void setRequired(boolean required);

    boolean isMergeable(FlowPropertyDefinition source);

    String getParameterName();

    PropertyRequired getPropertyRequired();
    void setPropertyRequired(PropertyRequired propertyRequired);

    boolean isSaveBack();

    String getInitial();

    /**
     * @return if true, the initial value of the property can be overridden by a
     * passed in value.
     */
    boolean isInitialMode();

    /**
     * @return all possible names for this FlowPropertyDefinition, including
     * {@link #getName()}.
     */
    Set<String> getAlternates();

    /**
     * @return how to use the property.
     */
    PropertyUsage getPropertyUsage();

    void setPropertyUsage(PropertyUsage propertyUsage);

    String getValidators();

    boolean isAssignableFrom(Class<?> clazz);

    boolean isRequired();
}
