/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.translator;

import org.amplafi.json.renderers.EnumJsonRenderer;
import org.amplafi.flow.FlowTranslator;

/**
 * @author patmoore
 * @param <T>
 *
 */
public class EnumFlowTranslator<T> extends AbstractFlowTranslator<T> implements InstanceSpecificFlowTranslator<T>{

    private Class<T> translatedClass;

    public EnumFlowTranslator() {
        super(EnumJsonRenderer.INSTANCE);
        this.translatedClass = (Class<T>) Enum.class;
    }
    public EnumFlowTranslator(EnumFlowTranslator<?> original, Class<T> translatedClass) {
        super(original);
        this.jsonRenderer = EnumJsonRenderer.INSTANCE;
        this.translatedClass = translatedClass;
    }

    /**
     * @return the translatedClass
     */
    @Override
    public Class<T> getTranslatedClass() {
        return translatedClass;
    }
    /**
     * @see org.amplafi.flow.translator.InstanceSpecificFlowTranslator#resolveFlowTranslator(java.lang.Class)
     */
    @Override
    public <V> FlowTranslator<V> resolveFlowTranslator(Class<V> clazz) {
        EnumFlowTranslator<V> narrowed = new EnumFlowTranslator<V>(this, clazz);
        return narrowed;
    }
}