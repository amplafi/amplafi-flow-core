package org.amplafi.flow.translator;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test {@link IntegerFlowTranslator}.
 * @author Andreas Andreou
 */
public class TestIntegerFlowTranslator extends AbstractTestFlowTranslators {
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        IntegerFlowTranslator translator = new IntegerFlowTranslator();
        translator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return translator;
    }

    @DataProvider(name = "flowTranslatorExpectations")
    @Override
    protected Object[][] getFlowTranslatorExpectations() {
        return new Object[][]{
                data(1, "1"),
                data(-1, "-1"),
                data(0, "0"),
                data(null, null),
                data(Integer.MAX_VALUE, "2147483647"),
        };
    }
}
