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
package org.amplafi.flow.launcher;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.sworddance.util.UriFactoryImpl;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static com.sworddance.util.UriFactoryImpl.*;
/**
 * @author patmoore
 *
 */
public class TestFlowLauncherLinkGenerator {

    private static final String SERVICE_ID = "flow";
    public static final URI BASE = UriFactoryImpl.createUri("http://sworddance.com/");
    public static final String FLOW_TYPE_NAME_1 = "flowType1";
    private static final String EXISTING_FLOW_ID = "ftsehkjhs";
    @Test(dataProvider="testLaunchers")
    public void testLinkGeneration(URI base, FlowLauncher flowLauncher, URI expectedResult) {
        FlowLauncherLinkGenerator flowLauncherLinkGenerator = new FlowLauncherLinkGeneratorImpl(SERVICE_ID);
        URI actual = flowLauncherLinkGenerator.createURI(base, flowLauncher);
        assertEquals(actual, expectedResult);
    }
    @DataProvider(name="testLaunchers")
    public Object[][] getTestLaunchers() {
        Map<String, String> initialFlowState_1 = null;
        Map<String, String> initialFlowState_2 = new HashMap<String, String>();
        initialFlowState_2.put("param1", "value_1");
        return new Object[][] {
            new Object[] {
                BASE, new StartFromDefinitionFlowLauncher(FLOW_TYPE_NAME_1, initialFlowState_1), BASE.resolve(createUri(SERVICE_ID+"/"+FLOW_TYPE_NAME_1))},
            new Object[] {
                BASE, new StartFromDefinitionFlowLauncher(FLOW_TYPE_NAME_1, initialFlowState_2), BASE.resolve(createUri(SERVICE_ID+"/"+FLOW_TYPE_NAME_1+"?"+createQueryString(initialFlowState_2)))},
            new Object[] {
                BASE, new ContinueFlowLauncher(FLOW_TYPE_NAME_1, EXISTING_FLOW_ID), BASE.resolve(createUri(SERVICE_ID+"/"+FLOW_TYPE_NAME_1+"/"+EXISTING_FLOW_ID))},
            new Object[] {
                null, new StartFromDefinitionFlowLauncher(FLOW_TYPE_NAME_1, initialFlowState_1), createUri("/"+SERVICE_ID+"/"+FLOW_TYPE_NAME_1)},
            new Object[] {
                null, new StartFromDefinitionFlowLauncher(FLOW_TYPE_NAME_1, initialFlowState_2),  createUri("/"+SERVICE_ID+"/"+FLOW_TYPE_NAME_1+"?"+createQueryString(initialFlowState_2))},
            new Object[] {
                null, new ContinueFlowLauncher(FLOW_TYPE_NAME_1, EXISTING_FLOW_ID), createUri("/"+ SERVICE_ID+"/"+FLOW_TYPE_NAME_1+"/"+EXISTING_FLOW_ID)}
        };
    }
}
