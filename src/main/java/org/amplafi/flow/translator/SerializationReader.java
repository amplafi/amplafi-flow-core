package org.amplafi.flow.translator;

import org.amplafi.flow.FlowTranslatorResolver;

/**
 * Implementors
 * @author patmoore
 *
 */
public interface SerializationReader extends FlowRendererProvider {

    void setFlowRendererProvider(FlowRendererProvider flowRendererProvider);
    void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver);
    <R extends SerializationReader> R optObject(String key);
    /**
     * The FlowRenderer returned knows about the SerializationReader that returned it.
     * this method is called by the {@link FlowTranslator}
     * @see org.amplafi.flow.translator.FlowRendererProvider#getFlowRenderer(java.lang.Class)
     */
    <T, C> FlowRenderer<T> getFlowRenderer(Class<? extends C> clazz);
}
