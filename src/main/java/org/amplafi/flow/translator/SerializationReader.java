package org.amplafi.flow.translator;

import org.amplafi.flow.FlowTranslatorResolver;

/**
 * Used to deserialize a request
 * @author patmoore
 *
 */
public interface SerializationReader extends FlowRendererProvider {

    void setFlowRendererProvider(FlowRendererProvider flowRendererProvider);
    void setFlowTranslatorResolver(FlowTranslatorResolver flowTranslatorResolver);
}
