/*
 * Created on Jan 6, 2008
 * Copyright 2006 by Amplafi, Inc.
 */
package org.amplafi.flow;

import org.amplafi.json.JSONWriter;
import org.amplafi.json.JsonRenderer;



/**
 * used to render a flow state as part of the api / flow service functionality.
 * @author Patrick Moore
 */
public class FlowStateJsonRenderer implements JsonRenderer<FlowState> {

    public static final String FS_PARAMETERS = "fsParameters";
    public static final String FS_LOOKUP_KEY = "fsLookupKey";
    public static final String FS_CURRENT_ACTIVITY_BY_NAME = "fsCurrentActivityByName";
    public static final String FS_COMPLETE = "fsComplete";

    @Override
    public Class<FlowState> getClassToRender() {
        return FlowState.class;
    }

    @Override
    public JSONWriter toJson(JSONWriter jsonWriter, FlowState flowState) {
        jsonWriter.object();
        jsonWriter.key(FS_COMPLETE).value(flowState.isCompleted());
        if (flowState.isActive()) {
            jsonWriter.key(FS_CURRENT_ACTIVITY_BY_NAME).value(flowState.getCurrentActivityByName());
        }
        jsonWriter.keyValueIfNotBlankValue(FS_LOOKUP_KEY, flowState.getLookupKey());

        FlowValuesMap flowValuesMap = flowState.getFlowValuesMap();
        if ( flowValuesMap != null && !flowValuesMap.isEmpty() ) {
            jsonWriter.key(FS_PARAMETERS).value(flowValuesMap.getAsFlattenedStringMap());
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
