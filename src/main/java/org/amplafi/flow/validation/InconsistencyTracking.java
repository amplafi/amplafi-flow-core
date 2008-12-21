package org.amplafi.flow.validation;

/**
 * {@link org.amplafi.flow.validation.FlowValidationTracking Tracking} for
 * incosistent data.<p/>
 * This should be typically used for data that shouldn't
 * coexist (male and pregnant) or that are simply invalid (negative age).
 * In all cases, a description of each field participating in this tracking
 * should be provided.
 * <p/>
 * The generated message of this tracking will stem from the key
 * "InconsistencyTracking.[detailKey]" (where detailKey is specified by user)
 * and will use the rest of the parameters passed in the constructor. 
 */
public class InconsistencyTracking extends SimpleValidationTracking {

    public InconsistencyTracking(String detailKey, String...value) {
        super(InconsistencyTracking.class.getSimpleName() + "." + detailKey, value);
    }
}