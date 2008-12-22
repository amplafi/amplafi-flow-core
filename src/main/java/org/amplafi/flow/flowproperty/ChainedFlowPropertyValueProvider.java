/**
 * Copyright 2006-2008 by Amplafi. All rights reserved. Confidential.
 */
package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * FlowPropertyValueProvider where many FlowPropertyValueProvider may be contributing to a single
 * property's value.
 *
 * @author patmoore
 * @param <FA> the expected FlowActivity
 */
public interface ChainedFlowPropertyValueProvider<FA extends FlowActivity> extends FlowPropertyValueProvider<FA> {
    /**
     * Set the previous {@link FlowPropertyValueProvider} for this chain.
     * @param previous the previous {@link FlowPropertyValueProvider}
     */
    public void setPrevious(FlowPropertyValueProvider<FA> previous);
}
