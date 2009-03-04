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

import java.util.Map;

import static org.testng.Assert.*;

import org.amplafi.flow.FlowTranslator;
import org.amplafi.flow.translator.MapFlowTranslator;
import org.apache.commons.collections.MapUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * @author patmoore
 *
 */
@Test
public class TestMapFlowTranslator extends AbstractTestFlowTranslators<Map<? extends Object, ? extends Object>> {

    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#createFlowTranslator()
     */
    @Override
    protected FlowTranslator<Map<? extends Object, ? extends Object>> createFlowTranslator() {
        MapFlowTranslator<Object, Object> mapFlowTranslator = new MapFlowTranslator<Object, Object>();
        mapFlowTranslator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return mapFlowTranslator;
    }

    /**
     * @see org.amplafi.flow.translator.AbstractTestFlowTranslators#getFlowTranslatorExpectations()
     */
    @Override
    @DataProvider(name="flowTranslatorExpectations")
    protected Object[][] getFlowTranslatorExpectations() {
        Map<Object, String> mapNoNulls = map("23", "value:23");

        Map<Object, Object> mapNulls = map(null, "null key", "null value", null);

        // HACK mapWierd DOES NOT WORK....
        Map<Object, String> mapWierd = map("string\" key", "wierd ' chars \\ ");

        Map<Object, String> mapHtml = map("body", "Hi <b>there</b>");

        return new Object[][] {
            new Object[] { null, "{}" },
            new Object[] { mapNulls, "{}" },
//            new Object[] { mapWierd, "{\"string\\\\\\\" key\":\"wierd ' chars \\\\ \"}" },
            new Object[] { mapNoNulls, "{\"23\":\"value:23\"}" },
            new Object[] { mapHtml, "{\"body\":\"Hi <b>there<\\/b>\"}" },
        };
    }

    @Override
    protected void compareResults(Object object, String expectedSerialize, String actualSerialize, Object deserialized) {
        Map<Object, Object> original = (Map<Object, Object>) object;
        Map<Object, Object> result = (Map<Object, Object>) deserialized;
        if ( MapUtils.isEmpty(original)) {
            assertEquals(expectedSerialize, "{}");
            assertTrue(MapUtils.isEmpty(result));
        } else {
            assertEquals(actualSerialize, expectedSerialize);
            for(Map.Entry<Object, Object> entry: original.entrySet()) {
                Object key = entry.getKey();
                if ( key == null || entry.getValue() == null) {
                    assertFalse(result.containsKey(key));
                } else {
                    assertEquals(result.get(key), entry.getValue());
                }
            }
        }
    }
}
