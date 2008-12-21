package org.amplafi.flow.validation;

/**
 * Tracks flow validation problems.
 */
public interface FlowValidationTracking {
    String getKey();
    String[] getParameters();
}
