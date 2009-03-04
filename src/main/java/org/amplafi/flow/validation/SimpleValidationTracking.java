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

import org.apache.commons.lang.StringUtils;
import org.amplafi.flow.FlowValidationTracking;

/**
 * General purpose {@link org.amplafi.flow.FlowValidationTracking}.
 */
public class SimpleValidationTracking implements FlowValidationTracking {

    private String key;
    private String[] params;

    public SimpleValidationTracking(String key, String... params) {
        this.key = key;
        this.params = params;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String[] getParameters() {
        return params;
    }
    @Override
    public String toString() {
        return getKey()+"["+StringUtils.join(params, ",")+"]";
    }
}
