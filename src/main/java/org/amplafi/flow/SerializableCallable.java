/*
 * Created on Jun 22, 2007
 * Copyright 2006 by Patrick Moore
 */
package org.amplafi.flow;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface SerializableCallable<V> extends Serializable, Callable<V> {

}
