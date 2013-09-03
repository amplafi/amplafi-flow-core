package org.amplafi.flow.flowproperty;

import java.util.Arrays;
import java.util.List;

import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * Used when a property can be retrieved via multiple properties.
 * For example, "messagePoint" which can be "messageSourcePoint" or "messageEndPoint"
 * @author patmoore
 *
 * @param <V>
 */
public class OneOfManyFlowPropertyValueProvider<V> extends AbstractFlowPropertyValueProvider<FlowPropertyProviderWithValues> implements FlowPropertyDefinitionProvider, FlowPropertyValueProvider<FlowPropertyProviderWithValues> {

    private List<Object> alternativeProperties;
    public OneOfManyFlowPropertyValueProvider(Class<? extends V> baseClass, Object...alternativeProperties) {
        super(new FlowPropertyDefinitionBuilder(baseClass));
        this.alternativeProperties = Arrays.asList(alternativeProperties);
    }
    public OneOfManyFlowPropertyValueProvider(String propertyName, Class<? extends V> baseClass, Object...alternativeProperties) {
        super(new FlowPropertyDefinitionBuilder(propertyName, baseClass));
        this.alternativeProperties = Arrays.asList(alternativeProperties);
    }
    @Override
    public <T> T get(FlowPropertyProviderWithValues flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
        T result = null;
        for(Object alternativeProperty : this.alternativeProperties) {
            if ( alternativeProperty instanceof Class) {
                result = (T)getProperty(flowPropertyProvider, flowPropertyDefinition, (Class)alternativeProperty);
            } else {
                result = (T) getProperty(flowPropertyProvider, flowPropertyDefinition, alternativeProperty.toString());
            }
            if ( result != null) {
                break;
            }
        }
        return result;
    }

}
