package org.amplafi.flow.validation;

/**
 * {@link org.amplafi.flow.FlowValidationTracking Tracking} for missing required values.
 * <p/>
 * A required field has a null value.
 * <p/>
 * The generated message of this tracking will stem from the key "MissingRequiredTracking" and the
 * parameter passed in the constructor.
 */
public class MissingRequiredTracking extends SimpleValidationTracking {
    /**
     * @param missing The missing field
     */
    public MissingRequiredTracking(String missing) {
        super(MissingRequiredTracking.class.getSimpleName(), missing);
    }
}
