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

import org.amplafi.flow.DataClassDefinition;
import org.amplafi.flow.flowproperty.DataClassDefinitionImpl;
import org.testng.annotations.Test;


import static org.testng.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test {@link DataClassDefinitionImpl}.
 */
public class TestDataClassDefinition {
    private static final boolean TEST_ENABLED = true;
    @Test
    public void testMap() {
        DataClassDefinitionImpl def = DataClassDefinitionImpl.map(Integer.class, String.class);
        assertEquals(def.getCollection(), Map.class);
        assertEquals(def.getDataClass(), Map.class);
        assertEquals(def.getKeyDataClassDefinition().getDataClass(), Integer.class);
        assertEquals(def.getElementDataClassDefinition().getDataClass(), String.class);
    }

    /**
     * Test {@link DataClassDefinition} for simple collections
     */
    @Test(enabled=TEST_ENABLED)
    public void testDataClassDefinition() {
        DataClassDefinitionImpl dataClassDefinition =
            new DataClassDefinitionImpl(Boolean.class, Set.class);
        assertEquals(dataClassDefinition.getElementDataClassDefinition().getDataClass(), Boolean.class);
        assertEquals(dataClassDefinition.getDataClass(), Set.class);
    }

    /**
     * Test {@link DataClassDefinition} for more complex collections (Map of lists )
     */
    @Test(enabled=TEST_ENABLED)
    public void testDataClassDefinitionCollection() {
        DataClassDefinitionImpl dataClassDefinition =
            new DataClassDefinitionImpl(Boolean.class, Set.class);
        assertTrue(dataClassDefinition.isCollection());
        dataClassDefinition =
            new DataClassDefinitionImpl(Set.class);
        assertTrue(dataClassDefinition.isCollection());
        assertFalse(dataClassDefinition.isMap());

        dataClassDefinition =
            new DataClassDefinitionImpl(Map.class);
        assertTrue(dataClassDefinition.isCollection());
        assertTrue(dataClassDefinition.isMap());
        assertEquals(dataClassDefinition.getDataClass(), Map.class );
        dataClassDefinition =
            new DataClassDefinitionImpl(List.class);
        assertTrue(dataClassDefinition.isCollection());
        assertFalse(dataClassDefinition.isMap());
    }
}
