package org.amplafi.flow.translator;

import org.amplafi.flow.translator.CharSequenceFlowTranslator;
import org.amplafi.flow.FlowTranslator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Test {@link CharSequenceFlowTranslator}.
 */
public class TestCharSequenceFlowTranslator extends AbstractTestFlowTranslators {
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        CharSequenceFlowTranslator flowTranslator = new CharSequenceFlowTranslator();
        flowTranslator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return flowTranslator;
    }

    @DataProvider(name = "flowTranslatorExpectations")
    @Override
    protected Object[][] getFlowTranslatorExpectations() {
        return new Object[][] {
                twins(""),
                twins("hello"),
                data("hello <b>there</b>", "hello <b>there<\\/b>"),
                data("he//llo <b>th\\ere</b>", "he//llo <b>th\\\\ere<\\/b>"),
                data("wierd ' chars \\ ", "wierd ' chars \\\\ "),
        };
    }
}
