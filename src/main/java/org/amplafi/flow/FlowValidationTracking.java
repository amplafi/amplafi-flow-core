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
package org.amplafi.flow;

/**
 * Tracks flow validation problems.
 */
public interface FlowValidationTracking {
    /**
     *
     * @return the activity key of the FlowActivity that should be activated if the user is
     * expected to fix this validation. May be null.
     */
    String getActivityKey();
    /**
     *
     * @return the message key which is used to look up the actual localized validation message.
     */
    String getMessageKey();
    /**
     *
     * @return message parameters which are used to format the message string to display to the user.
     */
    Object[] getMessageParameters();
}
