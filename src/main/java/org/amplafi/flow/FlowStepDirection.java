/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

/**
 * the direction the user is going in the flow.
 * @author patmoore
 *
 */
public enum FlowStepDirection {
    forward,
    backward,
    inPlace;

    public static FlowStepDirection get(int current, int next) {
        if ( current == next) {
            return inPlace;
        } else if ( current > next) {
            return backward;
        } else {
            return forward;
        }
    }
}
