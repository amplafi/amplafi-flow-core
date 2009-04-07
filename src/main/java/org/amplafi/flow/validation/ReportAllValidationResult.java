/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.amplafi.flow.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.amplafi.flow.FlowValidationResult;
import org.amplafi.flow.FlowValidationTracking;

/**
 * A {@link org.amplafi.flow.FlowValidationResult} that considers
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
     * @param messageKey
     * @param messageParams
     */
    @Override
    public FlowValidationResult addTracking(boolean valid, String messageKey, Object...messageParams) {
        if (!valid) {
            addTracking(new SimpleValidationTracking(messageKey, messageParams));
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
