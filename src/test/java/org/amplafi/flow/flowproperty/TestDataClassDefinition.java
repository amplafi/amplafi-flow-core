package org.amplafi.flow.flowproperty;

import org.amplafi.flow.flowproperty.DataClassDefinitionImpl;
import org.testng.annotations.Test;


import static org.testng.Assert.*;

import java.util.Map;

/**
 * Test {@link DataClassDefinitionImpl}.
 */
public class TestDataClassDefinition {
    @Test
    public void testMap() {
        DataClassDefinitionImpl def = DataClassDefinitionImpl.map(Integer.class, String.class);
        assertEquals(def.getCollection(), Map.class);
        assertEquals(def.getDataClass(), Map.class);
        assertEquals(def.getKeyDataClassDefinition().getDataClass(), Integer.class);
        assertEquals(def.getElementDataClassDefinition().getDataClass(), String.class);
    }
}
