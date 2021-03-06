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

import java.net.URI;


/**
 * {@link org.amplafi.flow.validation.FlowValidationTracking Tracking} for missing required values.
 * 
 * A required field has a null value.
 *
 * The generated message of this tracking will stem from the key "MissingRequiredTracking" and the
 * parameter passed in the constructor.
 */
public class MissingRequiredTracking extends SimpleValidationTracking {
    
    private URI redirectUri;
    /**
     * @param params The missing field
     */
    public MissingRequiredTracking(Object... params) {
        super(MissingRequiredTracking.class.getSimpleName(), params);
    }
    
    public MissingRequiredTracking(URI redirectUri, Object... params) {
        this(params);
        this.redirectUri = redirectUri;
    }
    /**
     * Helps describing 'missing value' problems.
     *
     * @param flowValidationResult keeps track of validation results
     * @param missingRequiredValue if true then the property is *NOT set correctly and we need
     *        a {@link MissingRequiredTracking}.
     * @param properties missing property's name
     * @return flowValidationResult
     */
    public static FlowValidationResult appendRequiredTrackingIfTrue(FlowValidationResult flowValidationResult, boolean missingRequiredValue, Object... properties) {
       return appendRequiredTrackingIfTrue(flowValidationResult, missingRequiredValue, null, properties);
    }
    
    public static FlowValidationResult appendRequiredTrackingIfTrue(FlowValidationResult flowValidationResult, boolean missingRequiredValue, URI redirectUri, Object... properties) {
        if (missingRequiredValue) {
            flowValidationResult.addTracking(new MissingRequiredTracking(redirectUri, properties));
        }
        return flowValidationResult;
    }

    @Override
    public URI getRedirectUri() {
        return redirectUri;
    }
}
