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

package org.amplafi.flow.web;

import java.io.IOException;
import java.util.Map;

import org.amplafi.flow.FlowNotFoundException;


/**
 *
 */
public interface FlowService {
    /**
     * Continues a flow.
     *
     * @param flowLookupKey the key of an existing flow to continue
     * @param propertyChanges values with which to update the state of the flow
     * @throws FlowRedirectException
     */
    public void continueFlowState(String flowLookupKey,
            Map<String, String> propertyChanges) throws FlowRedirectException;
    public void service(FlowRequest flowRequest) throws IOException, FlowNotFoundException, FlowRedirectException;
}
