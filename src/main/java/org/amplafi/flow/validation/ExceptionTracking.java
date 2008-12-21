package org.amplafi.flow.validation;

/**
 * {@link org.amplafi.flow.validation.FlowValidationTracking Tracking} for
 * exceptions.<p/>
 * This should be used only when there's no other way to correctly categorize
 * the desired tracking.
 * <p/>
 * The generated message of this tracking will stem from the key "ExceptionTracking"
 * using the string and the message of the exception passed in the constructor.  
 */
public class ExceptionTracking extends SimpleValidationTracking {
    public ExceptionTracking(String property, Exception e) {
        super(ExceptionTracking.class.getSimpleName(), property, e.getMessage());
    }
}
