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

import org.amplafi.flow.validation.ExceptionTracking;
import org.amplafi.flow.FlowValidationResult;
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

    public void testMerge() {
        FlowValidationResult result = new ReportAllValidationResult(
            new MissingRequiredTracking("firstname"));

        FlowValidationResult result1 = new ReportAllValidationResult();
        result1.merge(result);
        assertFalse(result1.isValid());

    }
}
