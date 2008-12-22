/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

/**
 * @author patmoore
 *
 */
public enum PropertyRequired {
    optional,
    /**
     * FlowProperty is required to be set before calling the {@link org.amplafi.flow.FlowActivity#activate()}.
     * Use for properties that must be set by prior steps for the FlowActivity to function correctly.
     *
     * Properties of this sort should be minimized. "activate" properties impose an ordering that may not
     * be the best for a user experience and restrict a product manager's ability to redesign a flow.
     */
    activate,
    /**
     * FlowProperty is required to be set before advancing beyond the {@link org.amplafi.flow.FlowActivity}.
     * This is used when *the* current FlowActivity is requesting a value that must be set.
     *
     * The FlowActivity must have the ability to let user set the property in this case.
     * Do not use as a gatekeeper to 'protect' later FlowActivities.
     */
    advance,
    /**
     * FlowProperty is required to be set prior to calling the {@link org.amplafi.flow.FlowActivity#saveChanges()}.
     *
     * This is used in cases where the FlowActivity can gather its needed data and perform correctly
     * until the moment that the changes need to be saved.
     */
    saveChanges,
    /**
     * FlowProperty is required to be set prior to calling the {@link org.amplafi.flow.FlowActivity#finishFlow(org.amplafi.flow.FlowState)}.
     */
    finish,
    /**
     * The FlowActivity will create it.
     */
    creates
}
