package org.amplafi.flow.translator;

import java.net.URI;

import org.amplafi.flow.translator.FlowTranslator;
import org.amplafi.flow.translator.UriFlowTranslator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


@Test
public class TestUriFlowTranslator extends AbstractTestFlowTranslators {
    @Override
    @DataProvider(name="flowTranslatorExpectations")
    public Object[][] getFlowTranslatorExpectations() {
        return new Object[][] {
            new Object[] { URI.create("www.amplafi.net"), "www.amplafi.net" },
            new Object[] { URI.create("http://www.amplafi.net"), "http://www.amplafi.net" }
        };
    }

    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#createFlowTranslator()
     */
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        UriFlowTranslator uriFlowTranslator = new UriFlowTranslator();
        uriFlowTranslator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return uriFlowTranslator;
    }
}
