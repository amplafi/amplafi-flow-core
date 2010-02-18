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
package org.amplafi.flow.flowproperty;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.Map;
import java.util.HashMap;

/**
 * Test {@link AddToMapFlowPropertyValueProvider}.
 */
public class TestAddToMapFlowPropertyValueProvider extends Assert {
    @Test
    public void testPrevious() {
        Map<String, String> map = new HashMap<String, String>(){{
            this.put("key1", "value1");
            this.put("key2", "value2");
        }};

        AddToMapFlowPropertyValueProvider<FlowPropertyProvider, String, String> provider =
                new AddToMapFlowPropertyValueProvider<FlowPropertyProvider, String, String>(map);
        Map<String, String> result = provider.get(null, null);
        assertEquals(result.size(), 2);
        assertEquals(result.get("key1"), "value1");

        AddToMapFlowPropertyValueProvider<FlowPropertyProvider, String, String> provider2 =
                new AddToMapFlowPropertyValueProvider<FlowPropertyProvider, String, String>();
        Map<String, String> result2 = provider2.get(null, null);
        assertEquals(result2.size(), 0);

        provider2.setPrevious(provider);
        result2 = provider2.get(null, null);
        assertEquals(result2.size(), 2);
        assertEquals(result2.get("key1"), "value1");
    }
}
