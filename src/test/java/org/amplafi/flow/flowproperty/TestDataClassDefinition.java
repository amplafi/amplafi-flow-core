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
