package org.amplafi.flow;

/**
 * Tracks flow validation problems.
 */
public interface FlowValidationTracking {
    String getKey();
    String[] getParameters();
}
