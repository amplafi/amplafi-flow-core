package org.amplafi.flow.validation;

import org.apache.commons.lang.StringUtils;

/**
 * General purpose {@link org.amplafi.flow.validation.FlowValidationTracking}.
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
