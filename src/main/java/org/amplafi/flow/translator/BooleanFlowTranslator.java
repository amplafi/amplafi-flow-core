/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package org.amplafi.flow.translator;

import org.amplafi.flow.FlowActivity;
import org.amplafi.json.renderers.BooleanJsonRenderer;


/**
 *
 *
 */
public class BooleanFlowTranslator extends AbstractFlowTranslator<Boolean> {

    public BooleanFlowTranslator() {
        super(BooleanJsonRenderer.INSTANCE);
        addDeserializedFormClasses(boolean.class);
    }

    /**
     * @see org.amplafi.flow.translator.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<Boolean> getTranslatedClass() {
        return Boolean.class;
    }

    @Override
    public Boolean getDefaultObject(FlowActivity flowActivity) {
        return Boolean.FALSE;
    }
}
