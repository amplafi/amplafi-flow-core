/*
 * Created on May 31, 2007
 * Copyright 2006 by Patrick Moore
 */
package org.amplafi.flow.launcher;

import java.util.Map;

import org.amplafi.flow.Flow;
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
     * @return map of initial parameters that will be set to the {@link FlowState} of the {@link Flow} to launch.
     */
    public Map<String,String> getInitialFlowState();
}
