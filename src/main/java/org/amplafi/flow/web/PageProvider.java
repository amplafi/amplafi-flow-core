/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.web;

import org.amplafi.flow.FlowState;

/**
 * Implementors are adaptors between the application's web framework
 * and the amplafi-flow library.
 *
 * Implementors initialize the FlowState with information about which pages should be displayed
 * when the FlowState is displayed.
 * @author patmoore
 *
 */
public interface PageProvider {
    /**
     *
     * @param flowState
     */
    void initializePages(FlowState flowState);
}
