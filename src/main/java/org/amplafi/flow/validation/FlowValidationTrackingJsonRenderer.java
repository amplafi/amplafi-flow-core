/*
 * Created on Jan 7, 2008
 * Copyright 2006 by Amplafi, Inc.
 */
package org.amplafi.flow.validation;

import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;


/**
 * JsonRender to render validation results. This is used at least initially in the api code.
 *
 * @author Patrick Moore
 */
public class FlowValidationTrackingJsonRenderer implements JsonRenderer<FlowValidationTracking> {

    @Override
    public Class<FlowValidationTracking> getClassToRender() {
        return FlowValidationTracking.class;
    }

    @Override
    public JSONWriter toJson(JSONWriter jsonWriter, FlowValidationTracking flowValidationTracking) {
        jsonWriter.object();
        jsonWriter.key("key").value(flowValidationTracking.getKey());
        if ( flowValidationTracking.getParameters() != null ) {
            jsonWriter.key("parameters");
            jsonWriter.array();
            for(String parameter: flowValidationTracking.getParameters()) {
                jsonWriter.value(parameter);
            }
            jsonWriter.endArray();
        }
        return jsonWriter.endObject();
    }

    /**
     * @see org.amplafi.json.JsonRenderer#fromJson(java.lang.Class, java.lang.Object, Object...)
     */
    @Override
    public <K> K fromJson(Class<K> clazz, Object value, Object... parameters) {
        throw new UnsupportedOperationException();
    }

}
