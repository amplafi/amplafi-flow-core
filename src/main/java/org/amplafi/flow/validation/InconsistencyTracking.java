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

    public InconsistencyTracking(String detailKey, Object...value) {
        super(InconsistencyTracking.class.getSimpleName() + "." + detailKey, value);
    }
    /**
     * Helps describing 'incorrect value' problems.
     *
     * @param result keeps track of validation results
     * @param value if true then we need to inform of an inconsistency (using
     *        {@link InconsistencyTracking}) described be the key and data
     *        parameters.
     * @param key The key that describes the inconsistency.
     * @param data Additional values to use for generating the message that
     *        describes the problem.
     * @return result
     */
    public static FlowValidationResult appendInconsistencyTrackingIfTrue(FlowValidationResult result, boolean value,
            String key, Object... data) {
        if (value) {
            result.addTracking(new InconsistencyTracking(key, data));
        }
        return result;
    }
}