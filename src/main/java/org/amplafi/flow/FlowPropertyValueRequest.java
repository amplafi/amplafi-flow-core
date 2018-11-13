package org.amplafi.flow;

import java.util.Optional;

import org.amplafi.flow.flowproperty.FlowPropertyProvider;

import com.sworddance.util.map.NamespaceMapKey;

public interface FlowPropertyValueRequest {

    /**
     * The FlowPropertyProvider making the request.
     */
    public FlowPropertyProvider getFlowPropertyProvider();

    /**
     * Available flowTranslator to get the translators based on the source of the rawSerializedObject
     *
     */
    public FlowTranslatorResolver getFlowTranslatorResolver();

    /**
     * Property Key Or propertyClass needs to be supplied
     */
    public Optional<NamespaceMapKey> getPropertyKey();

    /**
     * Or property class
     */
    public Optional<Class<?>> getPropertyClass();

    /**
     * if there is a raw serialized object, may not be available. This is o.k. if the called FlowPropertyValueProvider
     * can create an instance of the needed object
     */
    public Optional<?> getRawSerializedObject();

    public FlowPropertyDefinition getFlowPropertyDefinition();

}
