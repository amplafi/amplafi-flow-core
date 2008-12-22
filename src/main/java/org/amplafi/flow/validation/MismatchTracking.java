package org.amplafi.flow.validation;

/**
 * {@link org.amplafi.flow.FlowValidationTracking Tracking} for
 * mismatching values between two objects.<p/>
 * This is typically used when two fields should be equal (like password and
 * password confirmation) but they're found not to be. In that case, just use:<p/>
 * <code>
 *      new MismatchTracking("password", "password confirmation")
 * </code>
 * <p/>
 * The generated message of this tracking will stem from the key "MismatchTracking"
 * and the two parameters passed in the constructor.
 */
public class MismatchTracking extends SimpleValidationTracking {
    public MismatchTracking(String first, String second) {
        super(MismatchTracking.class.getSimpleName(), first, second);
    }
}