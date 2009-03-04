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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@link AddToListFlowPropertyValueProvider}.
 */
public class TestAddToListFlowPropertyValueProvider extends Assert {
    @Test
    public void testPrevious() {
        AddToListFlowPropertyValueProvider<String> provider =
                new AddToListFlowPropertyValueProvider<String>("one", "two");

        List<String> result = provider.get(null, null);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), "one");
        assertEquals(result.get(1), "two");

        AddToListFlowPropertyValueProvider<String> provider2 =
                new AddToListFlowPropertyValueProvider<String>();

        List<String> result2 = provider2.get(null, null);
        assertEquals(result2.size(), 0);

        provider2.setPrevious(provider);
        result2 = provider2.get(null, null);
        assertEquals(result2.size(), 2);
        assertEquals(result.get(0), "one");
        assertEquals(result.get(1), "two");
    }
}