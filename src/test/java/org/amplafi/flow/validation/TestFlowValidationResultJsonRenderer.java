/*
 * Created on Jan 7, 2008
 * Copyright 2006 by Amplafi, Inc.
 */
package org.amplafi.flow.validation;

import org.amplafi.flow.validation.FlowValidationResultJsonRenderer;
import org.amplafi.flow.validation.FlowValidationTracking;
import org.amplafi.flow.validation.FlowValidationTrackingJsonRenderer;
import org.amplafi.flow.validation.ReportAllValidationResult;
import org.amplafi.json.JSONStringer;
import org.amplafi.json.JSONWriter;
import org.easymock.classextension.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestFlowValidationResultJsonRenderer extends Assert {

    private FlowValidationResultJsonRenderer flowValidationResultJsonRenderer = new FlowValidationResultJsonRenderer();
    private FlowValidationTrackingJsonRenderer flowValidationTrackingJsonRenderer = new FlowValidationTrackingJsonRenderer();

    @Test
    public void testNoValidationErrors() throws Exception {
        ReportAllValidationResult reportAll = new ReportAllValidationResult();
        JSONWriter jsonWriter = getJsonWriter();
        jsonWriter.object().key("validation").value(reportAll).endObject();
        assertEquals(jsonWriter.toString(),"{\"validation\":{}}");

        ReportAllValidationResult single = new ReportAllValidationResult(true, "foo");
        jsonWriter = getJsonWriter();
        jsonWriter.object().key("validation").value(single).endObject();
        assertEquals(jsonWriter.toString(),"{\"validation\":{}}");
    }

    @Test
    public void testValidationErrors() throws Exception {
        ReportAllValidationResult reportAll = new ReportAllValidationResult();
        FlowValidationTracking[] flowValidationTrackings = new FlowValidationTracking[] {
                EasyMock.createMock(FlowValidationTracking.class),
                EasyMock.createMock(FlowValidationTracking.class)
        };
        for(int i =0 ; i < flowValidationTrackings.length; i++) {
            flowValidationTrackings[i] = EasyMock.createMock(FlowValidationTracking.class);
            EasyMock.expect(flowValidationTrackings[i].getKey()).andReturn(Integer.toString(i)).anyTimes();
            EasyMock.expect(flowValidationTrackings[i].getParameters()).andReturn(new String[] {
                    i+" error-1",
                    i+" error-2"
            }).anyTimes();
        }
        EasyMock.replay(flowValidationTrackings);

        reportAll.addTracking(flowValidationTrackings[0]);
        reportAll.addTracking(flowValidationTrackings[1]);
        JSONWriter jsonWriter = getJsonWriter();
        jsonWriter.object().key("validation").value(reportAll).endObject();
        assertEquals(jsonWriter.toString(),
                "{\"validation\":{\"flowValidationTracking\":[{\"key\":\"0\",\"parameters\":[\"0 error-1\",\"0 error-2\"]},{\"key\":\"1\",\"parameters\":[\"1 error-1\",\"1 error-2\"]}]}}");

        ReportAllValidationResult single = new ReportAllValidationResult(false, "foo");
        jsonWriter = getJsonWriter();
        jsonWriter.object().key("validation").value(single).endObject();
        assertEquals(jsonWriter.toString(),
        "{\"validation\":{\"flowValidationTracking\":[{\"key\":\"foo\",\"parameters\":[\"foo\"]}]}}");
    }

    private JSONWriter getJsonWriter() {
        JSONWriter jsonWriter = new JSONStringer();
        jsonWriter.addRenderer(flowValidationResultJsonRenderer);
        jsonWriter.addRenderer(flowValidationTrackingJsonRenderer);
        return jsonWriter;
    }

}
