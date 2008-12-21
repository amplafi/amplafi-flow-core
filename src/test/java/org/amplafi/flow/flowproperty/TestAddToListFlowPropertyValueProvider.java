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