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

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.amplafi.flow.json.JSONObject;
import org.amplafi.flow.json.translator.JSONObjectFlowTranslator;

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
