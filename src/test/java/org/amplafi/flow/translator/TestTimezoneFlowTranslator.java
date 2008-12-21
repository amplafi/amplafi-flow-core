/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.translator;

import java.util.TimeZone;

import org.amplafi.flow.translator.FlowTranslator;
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
