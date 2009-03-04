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

import java.net.URI;

import org.amplafi.flow.FlowTranslator;
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
