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
 * {@link org.amplafi.flow.FlowValidationTracking Tracking} for
 * exceptions.<p/>
 * This should be used only when there's no other way to correctly categorize
 * the desired tracking.
 * <p/>
 * The generated message of this tracking will stem from the key "ExceptionTracking"
 * using the string and the message of the exception passed in the constructor.
 */
public class ExceptionTracking extends SimpleValidationTracking {
    public ExceptionTracking(String property, Exception e) {
        super(ExceptionTracking.class.getSimpleName(), property, e.getMessage(), e);
    }
}
