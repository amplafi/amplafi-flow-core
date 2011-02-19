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

import java.util.Map;
import java.util.concurrent.Callable;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;


/**
 * 'Closure' object to re-enter an active flow or start a new one.
 *
 * FlowLaunchers are used when it is desired to encapsulate the state surrounding starting/continuing a new flow
 * before it is known if the starting will actually happen.
 *
 * TODO:
 * move FlowLauncher to the flowclient code because the concept of launching a flow makes sense as a flow-client operation.
 * Not all methods may be able to be moved. Most should be able to be moved.
 *
 * There may need to be a server-side extension of FlowLauncher ( to handle fact that FlowState is still server-sidey )
 *
 * @author Patrick Moore
 */
public interface FlowLauncher extends Callable<FlowState> {
    public static final String FLOW_ID = "fid";
    public static final String ADVANCE_TO_END = "advance";

    public static final String AS_FAR_AS_POSSIBLE = "afap";
    /**
     * advance through the flow until either the flow completes or
     * the current {@link org.amplafi.flow.FlowActivity} is named with the advanceTo value.
     *
     * In future it may be considered an error to not have a matching {@link org.amplafi.flow.FlowActivity} name
     */
    public static final String ADV_FLOW_ACTIVITY = "fsAdvanceTo";
    /**
     * {@link #ADVANCE_TO_END} "advance" --> go through all remaining FlowActivities until the flow completes.
     * {@link #AS_FAR_AS_POSSIBLE} "afap" --> advance flow until it can be completed.
     */
    public static final String COMPLETE_FLOW = "fsCompleteFlow";
    public static final String FLOW_STATE_JSON_KEY = "flowState";
    FlowManagement getFlowManagement();
    void setFlowManagement(FlowManagement sessionFlowManagement);
    /**
     * enter the flow.
     * @return flowState May be null if for some reason the FlowLauncher determined it
     * was no longer valid. Returning null should not be treated as an error.
     *
     */
    FlowState call();
    String getLinkTitle();
    void setLinkTitle(String flowLabel);
    /**
     *
     * @return the flow type this {@link FlowLauncher launcher}.
     */
    String getFlowTypeName();
    void setFlowTypeName(String flowTypeName);

    /**
     * @return map of initial parameters that will be set to the {@link FlowState} of the {@link org.amplafi.flow.Flow} to launch.
     */
    Map<String,String> getInitialFlowState();

    /**
     * add to the flow values Map if the key does not exist.
     * @param key
     * @param defaultValue
     * @return the previous value.
     */
    String putIfAbsent(String key, String defaultValue);
    void putAll(Map<? extends String, ? extends String> map);
    /**
     * add to the flow values Map
     * @param key
     * @param value
     * @return the previous value
     */
    String put(String key, String value);
    /**
     * @param lookupKey
     */
    void setReturnToFlow(String lookupKey);
}
