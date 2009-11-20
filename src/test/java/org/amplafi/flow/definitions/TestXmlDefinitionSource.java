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
package org.amplafi.flow.definitions;

import java.util.Arrays;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
/**
 * @author patmoore
 *
 */
public class TestXmlDefinitionSource {

    @Test
    public void testLoadingFromExternalFile() {
        XmlDefinitionSource xmlDefinitionSource = new XmlDefinitionSource("src/main/resources/META-INF/flows/amplafi.suppliedflows.xml");
        assertFalse(xmlDefinitionSource.getFlowDefinitions().isEmpty());
    }
    @Test
    public void testLoadingFromIncludedResource() {
        XmlDefinitionSource xmlDefinitionSource;
        String file = "amplafi.suppliedflows.xml";
        for(String fileName: Arrays.asList(file, "META-INF/flows/"+file, "/flows/"+file, "flows/"+file)) {
            xmlDefinitionSource = new XmlDefinitionSource(fileName);
            assertFalse(xmlDefinitionSource.getFlowDefinitions().isEmpty());
        }
    }
}
