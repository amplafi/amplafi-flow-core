/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.translator;

import org.amplafi.flow.FlowTranslator;

/**
 * {@link FlowTranslator}s that should be cloned before being used.
 *
 */
public interface InstanceSpecificFlowTranslator<T> extends FlowTranslator<T>{
    public <V> FlowTranslator<V> resolveFlowTranslator(Class<V> clazz);
}
