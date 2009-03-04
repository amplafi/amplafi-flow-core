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