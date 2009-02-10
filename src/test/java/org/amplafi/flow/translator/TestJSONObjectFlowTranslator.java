package org.amplafi.flow.translator;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.amplafi.flow.FlowTranslator;
import org.amplafi.json.JSONObject;

/**
 * Test {@link JSONObjectFlowTranslator}.
 */
public class TestJSONObjectFlowTranslator extends AbstractTestFlowTranslators {
    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#createFlowTranslator()
     */
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        JSONObjectFlowTranslator flowTranslator = new JSONObjectFlowTranslator();
        flowTranslator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return flowTranslator;
    }

    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#getFlowTranslatorExpectations()
     */
    @Override
    @DataProvider(name="flowTranslatorExpectations")
    protected Object[][] getFlowTranslatorExpectations() {
        String json1 = "{\"count\":1,\"data\":\"test\"}";
        String json2 = "{}";
        return new Object[][] {
            new Object[] { new JSONObject(json1), json1 },
            new Object[] { new JSONObject(json2), json2 },
            new Object[] { null, null },
        };
    }
}
