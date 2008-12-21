package org.amplafi.flow.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * A {@link org.amplafi.flow.validation.FlowValidationResult} that considers
 * all trackings as problematic. This should currently be the desired behavior.
 */
public class ReportAllValidationResult implements FlowValidationResult {
    private List<FlowValidationTracking> trackings;

    public ReportAllValidationResult(FlowValidationTracking...flowValidationTrackings) {
        addTracking(flowValidationTrackings);
    }
    public ReportAllValidationResult(boolean valid, String key) {
        addTracking(valid, key, key);
    }
    public ReportAllValidationResult(boolean valid, String key, String value) {
        addTracking(valid, key, value);
    }
    @Override
    public boolean isValid() {
        return trackings==null || trackings.isEmpty();
    }

    /**
     * @param flowValidationTrackings nulls are filtered out.
     */
    @Override
    public FlowValidationResult addTracking(FlowValidationTracking... flowValidationTrackings) {
        if ( flowValidationTrackings != null ) {
            if (trackings==null) {
                trackings = new ArrayList<FlowValidationTracking>();
            }
            for (FlowValidationTracking flowValidationTracking: flowValidationTrackings) {
                if ( flowValidationTracking != null ) {
                    trackings.add(flowValidationTracking);
                }
            }
        }
        return this;
    }

    /**
     * add a {@link SimpleValidationTracking} if valid is false.
     * @param valid add if false
     * @param key
     * @param params
     */
    @Override
    public FlowValidationResult addTracking(boolean valid, String key, String...params) {
        if (!valid) {
            addTracking(new SimpleValidationTracking(key, params));
        }
        return this;
    }

    @Override
    public List<FlowValidationTracking> getTrackings() {
        return trackings;
    }

    @Override
    public String toString() {
        if(isValid()){
            return this.getClass().getSimpleName() + " no problems.";
        } else {
            return this.getClass().getSimpleName() + " failed. Trackings: " + StringUtils.join(this.getTrackings(), ';');
        }
    }
}
