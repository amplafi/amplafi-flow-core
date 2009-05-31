/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.flowproperty;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowPropertyValueProvider;

/**
 * @author patmoore
 * @param <FA>
 * @param <V>
 *
 */
public interface FutureFlowPropertyValueProvider<FA extends FlowActivity, V> extends Callable<V>, FlowPropertyValueProvider<FA>, Future<V> {

}
