package org.amplafi.flow.launcher;

import java.util.Map;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.amplafi.flow.SerializableCallable;


/**
 * 'Closure' object to re-enter an active flow or start a new one.
 *
 * FlowLaunchers are used when it is desired to encapsulate the state surrounding starting/continuing a new flow
 * before it is known if the starting will actually happen.
 * @author Patrick Moore
 */
public interface FlowLauncher extends SerializableCallable<FlowState> {
    public static final String FLOW_ID = "fid";
    // HACK needed until https://issues.apache.org/jira/browse/TAPESTRY-1876
    // is addressed.
    public static final String _KEY_LIST = "_key_List__";
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
    public void setFlowManagement(FlowManagement sessionFlowManagement);
    /**
     * enter the flow.
     * @return flowState May be null if for some reason the FlowLauncher determined it
     * was no longer valid. Returning null should not be treated as an error.
     *
     */
    public FlowState call();
    public String getFlowLabel();
    /**
     *
     * @return the flow type this {@link FlowLauncher} launches.
     */
    public String getFlowTypeName();

    /**
     * @return map of initial parameters that will be set to the {@link FlowState} of the {@link org.amplafi.flow.Flow} to launch.
     */
    public Map<String,String> getInitialFlowState();

    /**
     * add to the flow values Map if the key does not exist.
     * @param key
     * @param defaultValue
     * @return the previous value.
     */
    public String putIfAbsent(String key, String defaultValue);
    /**
     * add to the flow values Map
     * @param key
     * @param value
     * @return the previous value
     */
    public String put(String key, String value);
    /**
     * @param lookupKeyOrBoolean
     */
    public void setReturnToFlow(Object lookupKeyOrBoolean);
}
