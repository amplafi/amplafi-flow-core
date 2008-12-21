package org.amplafi.flow.validation;

import org.amplafi.flow.validation.ExceptionTracking;
import org.amplafi.flow.validation.FlowValidationResult;
import org.amplafi.flow.validation.InconsistencyTracking;
import org.amplafi.flow.validation.MismatchTracking;
import org.amplafi.flow.validation.MissingRequiredTracking;
import org.amplafi.flow.validation.ReportAllValidationResult;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Tests for {@link ReportAllValidationResult}.
 */
@Test
public class TestReportAllValidationResult extends Assert {
    public void testNothingAdded() {
        ReportAllValidationResult result = new ReportAllValidationResult(true, "key");
        assertTrue(result.isValid());

        ReportAllValidationResult result2 = new ReportAllValidationResult(true, "key", "val");
        assertTrue(result2.isValid());
    }

    public void testOneError() {
        FlowValidationResult result =
                new ReportAllValidationResult(false, "key", "val").addTracking(false, "key2");
        assertFalse(result.isValid());
    }

    public void testAddDifferentTrackings() {
        ReportAllValidationResult result = new ReportAllValidationResult(
                new MissingRequiredTracking("firstname"),
                new InconsistencyTracking("field not a number", "username"),
                new MismatchTracking("field1", "field2"),
                new ExceptionTracking("field", new RuntimeException())
        );

        assertFalse(result.isValid());
        assertEquals(result.getTrackings().size(), 4);
    }
}
