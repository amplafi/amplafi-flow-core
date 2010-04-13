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

package org.amplafi.flow.translator;

import java.util.Calendar;
import java.util.TimeZone;

import org.amplafi.flow.translator.CalendarFlowTranslator;
import org.amplafi.json.renderers.CalendarJsonRenderer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * @author patmoore
 *
 */
public class TestCalendarFlowTranslator extends AbstractTestFlowTranslators {

    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#createFlowTranslator()
     */
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        CalendarFlowTranslator calendarFlowTranslator = new CalendarFlowTranslator();
        calendarFlowTranslator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return calendarFlowTranslator;
    }

    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#getFlowTranslatorExpectations()
     */
    @Override
    @DataProvider(name="flowTranslatorExpectations")
    protected Object[][] getFlowTranslatorExpectations() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.clear();
        calendar.set(2008, 10, 9, 11, 57, 0);
        Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        calendar1.clear();
        calendar1.set(2008, 10, 9, 11, 57, 0);
        return new Object[][] {
            new Object[] { calendar, "{\""+CalendarJsonRenderer.TIME_IN_MILLIS+"\":1226231820000,\""+CalendarJsonRenderer.TIMEZONE_ID+"\":\""+"GMT"+"\"}" },
            new Object[] { calendar1, "{\""+CalendarJsonRenderer.TIME_IN_MILLIS+"\":1226260620000,\""+CalendarJsonRenderer.TIMEZONE_ID+"\":\""+"America/Los_Angeles"+"\"}" },
        };
    }

}
