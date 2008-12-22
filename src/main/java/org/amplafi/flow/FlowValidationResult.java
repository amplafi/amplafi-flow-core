package org.amplafi.flow;

import org.amplafi.flow.FlowValidationTracking;

import java.util.List;

/**
 * Holds flow validation results.
 */
public interface FlowValidationResult {
    /**
     * Checks for validation problems. This can be determined
     * from the existence of FlowValidationTrackings.
     *
     * @return true if there are no validation problems.
     */
    boolean isValid();

    /**
     * Adds a validation problem.
     *
     * @param tracking A validation issue to track.
     * @return this
     */
    FlowValidationResult addTracking(FlowValidationTracking... tracking);
    /**
     * add a {@link org.amplafi.flow.validation.SimpleValidationTracking} if valid is false.
     * @param valid add if false
     * @param key
     * @param params
     * @return this
     */
    public FlowValidationResult addTracking(boolean valid, String key, String...params);
    /**
     * Get all trackings.
     * @return all trackings stored. Null is allowed.
     */
    List<FlowValidationTracking> getTrackings();
}
