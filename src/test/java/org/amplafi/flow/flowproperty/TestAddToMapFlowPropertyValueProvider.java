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

        AddToMapFlowPropertyValueProvider<String, String> provider =
                new AddToMapFlowPropertyValueProvider<String, String>(map);
        Map<String, String> result = provider.get(null, null);
        assertEquals(result.size(), 2);
        assertEquals(result.get("key1"), "value1");

        AddToMapFlowPropertyValueProvider<String, String> provider2 =
                new AddToMapFlowPropertyValueProvider<String, String>();
        Map<String, String> result2 = provider2.get(null, null);
        assertEquals(result2.size(), 0);

        provider2.setPrevious(provider);
        result2 = provider2.get(null, null);
        assertEquals(result2.size(), 2);
        assertEquals(result2.get("key1"), "value1");
    }
}
