/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import org.amplafi.flow.FlowActivity;

/**
 * @author patmoore
 * @param <FA>
 * @param <V>
 *
 */
public abstract class AbstractFutureFlowPropertyValueProvider<FA extends FlowActivity, V> extends
    AbstractFlowPropertyValueProvider<FA>
    implements FutureFlowPropertyValueProvider<FA, V> {

}
