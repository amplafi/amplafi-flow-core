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

import java.util.HashMap;
import java.util.Map;
import static org.amplafi.flow.FlowConstants.FSRETURN_TO_FLOW;
import org.amplafi.flow.FlowManagement;

/**
 *
 *
 */
public abstract class BaseFlowLauncher implements FlowLauncher {
    private transient FlowManagement flowManagement;
    private String returnToFlowLookupKey;
    private Map<String, String> valuesMap;
    protected BaseFlowLauncher() {

    }
    /**
     * @param flowManagement
     */
    public BaseFlowLauncher(FlowManagement flowManagement, Map<String, String> valuesMap) {
        this.flowManagement = flowManagement;
        this.valuesMap = new HashMap<String, String>();
        if ( valuesMap != null) {
            this.valuesMap.putAll(valuesMap);
        }
    }
    @Override
    public void setFlowManagement(FlowManagement sessionFlowManagement) {
        this.flowManagement = sessionFlowManagement;
    }
    public FlowManagement getFlowManagement() {
        if(flowManagement == null) {
            throw new IllegalStateException("no flowmanagement object supplied!");
        }
        return flowManagement;
    }
    /**
     * @param lookupKeyOrBoolean the lookupKeyOrBoolean to set
     */
    public void setReturnToFlow(Object lookupKeyOrBoolean) {
        // TODO need to handle boolean
        // (idea is to return to the top-most flow at the time the flow launched by this flowlauncher completes (which may not have been the case when the flow started)
        put(FSRETURN_TO_FLOW, lookupKeyOrBoolean.toString());
        this.returnToFlowLookupKey = lookupKeyOrBoolean.toString();
    }
    /**
     * @return the lookupKeyOrBoolean
     */
    public String getReturnToFlow() {
        return returnToFlowLookupKey;
    }
    public Map<String, String> getValuesMap() {
        if ( this.valuesMap == null) {
            this.valuesMap = new HashMap<String, String>();
        }
        return this.valuesMap;
    }
    @Override
    public Map<String, String> getInitialFlowState() {
        return getValuesMap();
    }
    /**
     * @see org.amplafi.flow.launcher.FlowLauncher#put(java.lang.String, java.lang.String)
     */
    @Override
    public String put(String key, String value) {
        return this.getValuesMap().put(key, value);
    }
    /**
     * @see org.amplafi.flow.launcher.FlowLauncher#putIfAbsent(java.lang.String, java.lang.String)
     */
    @Override
    public String putIfAbsent(String key, String defaultValue) {
        if ( !this.getValuesMap().containsKey(key)) {
            return this.getValuesMap().put(key, defaultValue);
        } else {
            return null;
        }
    }
    public String getListDisplayValue() {
        return getFlowLabel();
    }

}
