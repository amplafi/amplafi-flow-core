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
package org.amplafi.flow.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.amplafi.flow.FlowState;

import com.sworddance.util.ApplicationIllegalStateException;
import com.sworddance.util.ApplicationNullPointerException;

import static org.apache.commons.lang.StringUtils.*;

public class SessionFlows implements Iterable<FlowStateImplementor>{
    private LinkedList<FlowStateImplementor> activeFlows = new LinkedList<FlowStateImplementor>();
    private Map<String, FlowStateImplementor> activeFlowsMap = new ConcurrentHashMap<String, FlowStateImplementor>();
    public synchronized boolean remove(FlowStateImplementor flowState) {
        activeFlowsMap.values().remove(flowState);
        return activeFlows.remove(flowState);
    }
    /**
     * @param lookupKey
     * @return the removed {@link FlowState} with the supplied lookupKey.
     */
    public synchronized FlowState removeByLookupKey(String lookupKey) {
        FlowState flowState = activeFlowsMap.remove(lookupKey);
        activeFlows.remove(flowState);
        return flowState;
    }
    /**
     * @return true if no active flows.
     */
    public synchronized boolean isEmpty() {
        return activeFlows.isEmpty();
    }

    public synchronized FlowStateImplementor getFirst() {
        return isEmpty()?null:activeFlows.getFirst();
    }
    public synchronized void addLast(FlowStateImplementor flowState) {
        activeFlowsMap.put(flowState.getLookupKey(), flowState);
        activeFlows.add(flowState);
    }
    public synchronized void makeFirst(FlowStateImplementor flowState) {
        if ( getFirst() == flowState) {
            return;
        } else if ( activeFlowsMap.containsKey(flowState.getLookupKey())) {
            activeFlows.remove(flowState);
        }
        activeFlowsMap.put(flowState.getLookupKey(), flowState);
        activeFlows.addFirst(flowState);
    }
    public synchronized int makeAfter(FlowStateImplementor flowState, FlowStateImplementor nextFlowState) {
        ApplicationIllegalStateException.checkState(activeFlowsMap.containsKey(flowState.getLookupKey()),
            flowState.getLookupKey()+ ": not a current flow");

        int oldPosition = -1;
        for(int i = 0; i < this.activeFlows.size(); ) {
            FlowStateImplementor state = this.activeFlows.get(i);
            if ( state == nextFlowState) {
                oldPosition = i;
                this.activeFlows.remove(i);
            } else {
                if ( state == flowState) {
                    this.add(++i, nextFlowState);
                }
                i++;
            }
        }
        return oldPosition;
    }
    public boolean add(FlowStateImplementor flowState) {
        addLast(flowState);
        return true;
    }
    /**
     * @param i
     * @param nextFlowState
     */
    public synchronized void add(int i, FlowStateImplementor nextFlowState) {
        this.activeFlows.add(i, nextFlowState);
        this.activeFlowsMap.put(nextFlowState.getLookupKey(), nextFlowState);
    }
    public synchronized FlowState get(String lookupKey) {
        ApplicationNullPointerException.notNull(lookupKey,"lookupKey for flow is null!");
        return this.activeFlowsMap.get(lookupKey);
    }
    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public synchronized Iterator<FlowStateImplementor> iterator() {
        return this.activeFlows.iterator();
    }

    @Override
    public String toString() {
        return "Session Flows : " + join(this.activeFlows, ", ");
    }
}