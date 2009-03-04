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

import java.util.TimeZone;

import org.amplafi.flow.FlowTranslator;
import org.amplafi.flow.translator.TimezoneFlowTranslator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * @author patmoore
 *
 */
public class TestTimezoneFlowTranslator extends AbstractTestFlowTranslators {

    @Override
    @DataProvider(name="flowTranslatorExpectations")
    public Object[][] getFlowTranslatorExpectations() {
        return new Object[][] {
            new Object[] { TimeZone.getTimeZone("GMT"), "GMT" },
            new Object[] { TimeZone.getTimeZone("America/Los_Angeles"), "America/Los_Angeles" }
        };
    }
    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#createFlowTranslator()
     */
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        TimezoneFlowTranslator timezoneFlowTranslator = new TimezoneFlowTranslator();
        timezoneFlowTranslator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return timezoneFlowTranslator;
    }
}
