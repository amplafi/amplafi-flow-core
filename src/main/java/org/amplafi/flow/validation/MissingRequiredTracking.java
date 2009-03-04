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
