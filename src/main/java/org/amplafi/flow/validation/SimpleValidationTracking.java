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

import org.apache.commons.lang.StringUtils;

/**
 * General purpose {@link org.amplafi.flow.validation.FlowValidationTracking}.
 */
public class SimpleValidationTracking implements FlowValidationTracking {

    private String activityKey;
    private String messageKey;
    private Object[] params;

    public SimpleValidationTracking(String messageKey, Object... params) {
        this.messageKey = messageKey;
        this.params = params;
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends SimpleValidationTracking> T initActivityKey(String activityKey) {
        this.setActivityKey(activityKey);
        return (T) this;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public Object[] getMessageParameters() {
        return params;
    }
    /**
     * @param activityKey the activityKey to set
     */
    public void setActivityKey(String activityKey) {
        this.activityKey = activityKey;
    }

    /**
     * @return the activityKey
     */
    public String getActivityKey() {
        return activityKey;
    }

    @Override
    public String toString() {
        return getMessageKey()+"["+StringUtils.join(params, ",")+"]";
    }

	@Override
	public URI getRedirectUri() {
		return null;
	}
}
