package org.amplafi.flow.translator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test {@link org.amplafi.flow.translator.ShortFlowTranslator}.
 * @author Andreas Andreou
 */
public class TestShortFlowTranslator extends AbstractTestFlowTranslators {
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        ShortFlowTranslator translator = new ShortFlowTranslator();
        translator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return translator;
    }

    @DataProvider(name = "flowTranslatorExpectations")
    @Override
    protected Object[][] getFlowTranslatorExpectations() {
        return new Object[][]{
                data((short)1, "1"),
                data((short)-1, "-1"),
                data((short)0, "0"),
                data(null, null),
                data(Short.MAX_VALUE, "32767"),
        };
    }
}