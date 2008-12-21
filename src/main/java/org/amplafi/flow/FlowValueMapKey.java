/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

/**
 * @author patmoore
 *
 */
public interface FlowValueMapKey extends CharSequence {
    public static final String NO_NAMESPACE = "";
    public String getNamespace();
    public String getKey();
}
