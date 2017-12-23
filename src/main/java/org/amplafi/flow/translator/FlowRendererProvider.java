package org.amplafi.flow.translator;

/**
 * Implementers supply the specific low-level renderers needed for serialization/deserialization.
 *
 * {@link FlowTranslator} implementations will ask for the correct primitive class needed to do
 * the low-level serialization or deserialization
 * @author patmoore
 *
 */
public interface FlowRendererProvider {
    /**
     *
     * @param clazz
     * @return the correct renderer for the class
     */
    <T,C> FlowRenderer<T> getFlowRenderer(Class<? extends C> clazz);
}
