/*
 * Created on Jan 7, 2008
 * Copyright 2006 by Amplafi, Inc.
 */
package org.amplafi.flow;

import org.amplafi.flow.FlowLifecycleState;
import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.FlowStateImpl;
import org.amplafi.flow.FlowStateJsonRenderer;
import org.amplafi.flow.validation.FlowValidationResultJsonRenderer;
import org.amplafi.flow.validation.FlowValidationTrackingJsonRenderer;
import org.amplafi.json.JSONStringer;
import org.amplafi.json.JSONWriter;
import org.amplafi.json.renderers.MapJsonRenderer;
import org.easymock.classextension.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestFlowStateJsonRenderer extends Assert {
    private FlowStateJsonRenderer flowStateJsonRenderer = new FlowStateJsonRenderer();
    private FlowValidationResultJsonRenderer flowValidationResultJsonRenderer = new FlowValidationResultJsonRenderer();
    private FlowValidationTrackingJsonRenderer flowValidationTrackingJsonRenderer = new FlowValidationTrackingJsonRenderer();
    private MapJsonRenderer mapJsonRenderer = new MapJsonRenderer();

    @Test
    public void testSimpleFlowState() {
        FlowState flowState = newFlowState();
        JSONWriter jsonWriter = getJsonWriter();
        jsonWriter.object().key("flowState").value(flowState).endObject();
        assertEquals(jsonWriter.toString(), "{\"flowState\":{\""+FlowStateJsonRenderer.FS_COMPLETE+"\":false}}");

    }
    @Test
    public void testCompleteFlowState() {
        FlowState flowState = newFlowState();
        flowState.setProperty("property1", "value1");
        flowState.setProperty("property2", "value2");
        flowState.setFlowLifecycleState(FlowLifecycleState.successful);
        JSONWriter jsonWriter = getJsonWriter();
        jsonWriter.object().key("flowState").value(flowState).endObject();
        assertEquals(jsonWriter.toString(), "{\"flowState\":{\""+FlowStateJsonRenderer.FS_COMPLETE+"\":true,\""+
                FlowStateJsonRenderer.FS_PARAMETERS+"\":{\"property1\":\"value1\",\"property2\":\"value2\"}}}");
    }

    private FlowState newFlowState() {
        FlowStateImpl flowState = new FlowStateImpl();
        FlowManagement flowManagement = EasyMock.createMock(FlowManagement.class);
        flowManagement.registerForCacheClearing();
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(flowManagement);
        flowState.setFlowManagement(flowManagement);
        return flowState;
    }
    private JSONWriter getJsonWriter() {
        JSONWriter jsonWriter = new JSONStringer();
        jsonWriter.addRenderer(flowValidationResultJsonRenderer);
        jsonWriter.addRenderer(flowValidationTrackingJsonRenderer);
        jsonWriter.addRenderer(flowStateJsonRenderer);
        jsonWriter.addRenderer(mapJsonRenderer);
        return jsonWriter;
    }
}
