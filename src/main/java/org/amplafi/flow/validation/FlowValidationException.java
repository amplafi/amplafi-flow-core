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

import java.util.List;

import org.amplafi.flow.FlowActivity;
import org.amplafi.flow.FlowValidationResult;
import org.amplafi.flow.FlowValidationTracking;
import org.amplafi.flow.FlowException;


/**
 * Exception thrown when there is a problem detected with the data already in the {@link org.amplafi.flow.FlowState},
 * or being passed into the flow to be saved. For example, trying to save a person's name in a property
 * expecting a number.
 *
 */
public class FlowValidationException extends FlowException {
    private final FlowValidationResult validationResult;

    public FlowValidationException(FlowValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public FlowValidationException(FlowValidationTracking...flowValidationTrackings) {
        super("Validation Problem");
        this.validationResult = new ReportAllValidationResult(flowValidationTrackings);
    }
    public FlowValidationException(Throwable cause, FlowValidationTracking...flowValidationTrackings) {
        this(flowValidationTrackings);
        initCause(cause);
    }

    public FlowValidationException(String key, FlowValidationTracking...flowValidationTrackings) {
        this(flowValidationTrackings);
        this.validationResult.addTracking(new SimpleValidationTracking(key));
    }

    /**
     * @param currentActivity
     * @param flowValidationResult
     */
    public FlowValidationException(FlowActivity currentActivity, FlowValidationResult flowValidationResult) {
        super(currentActivity.getFullActivityName());
        this.validationResult = flowValidationResult;
    }

    public List<FlowValidationTracking> getTrackings() {
        return this.validationResult.getTrackings();
    }
    @Override
    public FlowValidationException initCause(Throwable cause) {
        super.initCause(cause);
        setStackTrace(cause.getStackTrace());
        return this;
    }

    /**
     * @return the result causing the problem
     */
    public FlowValidationResult getResult() {
        return this.validationResult;
    }
    @Override
    public String toString() {
        return this.getMessage()+" : "+ this.validationResult.toString();
    }
}
