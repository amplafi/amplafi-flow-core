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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.apache.commons.lang.ObjectUtils;


import static org.apache.commons.collections.CollectionUtils.*;
import static org.apache.commons.lang.StringUtils.*;

/**
 * {@link FlowLauncher} that starts a new {@link FlowState}.
 * TODO:
 * Check login case. initialValues has email and password repeated
 * @author Patrick Moore
 */
public class StartFromDefinitionFlowLauncher extends BaseFlowLauncher implements ListableFlowLauncher {
    private static final long serialVersionUID = 7909909329479094947L;
    private List<String> initialValues;
    private transient Object propertyRoot;

    public StartFromDefinitionFlowLauncher() {

    }
    public StartFromDefinitionFlowLauncher(String flowTypeName, FlowManagement flowManagement) {
        this(flowTypeName, null, flowManagement);
    }
    public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
            FlowManagement flowManagement) {
        this(flowTypeName, initialFlowState, flowManagement, flowTypeName);
    }

    public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
            FlowManagement flowManagement, Serializable keyExpression) {
        super(flowTypeName, flowManagement, initialFlowState, keyExpression);
    }
    public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState, Serializable keyExpression) {
        super(flowTypeName, initialFlowState, keyExpression);
    }
    /**
    *
    * @param flowTypeName
     * @param initialFlowState map of strings - not evaluated like initialValues is.
     * @param flowManagement
     * @param keyExpression for html rendering identification.
     * @param propertyRoot must be provided if the initialValues need to be evaluated when the flow is launched.
     * @param initialValues used to define the initial values for flow. This is a
    * list of strings. Each string is 'key=value'. if value is the same name as a component
    * that has a 'value' attribute (like TextField components) then the initial value.
    * If value is a container's property then that value is used. Otherwise the value
    * provided is used as a literal.
    */
    public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState, FlowManagement flowManagement,
            Serializable keyExpression, Object propertyRoot, Iterable<String> initialValues) {
        this(flowTypeName, initialFlowState, flowManagement, keyExpression);
        this.setInitialValues(initialValues);
        this.propertyRoot = propertyRoot;
    }

    @Override
    public FlowState call() {
        FlowState flowState;
        Map<String,String> launchMap;
        if(this.initialValues != null && !this.initialValues.isEmpty()) {
            launchMap = new LinkedHashMap<String, String>();
            launchMap.putAll(getValuesMap());
            launchMap.putAll(convertToMap());
        } else {
            launchMap = getValuesMap();
        }
        flowState = getFlowManagement().startFlowState(getFlowTypeName(), true, launchMap, getReturnToFlow());
        return flowState;
    }

    @Deprecated // used only one place and that is questionable.
    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    public void setPropertyRoot(Object propertyRoot) {
        this.propertyRoot = propertyRoot;
    }
    public Object getPropertyRoot() {
        return propertyRoot;
    }
    public void setInitialValues(Iterable<String> initialValues) {
        this.initialValues = new ArrayList<String>();
        if ( initialValues != null ) {
            addAll(this.initialValues, initialValues.iterator());
        }
    }
    public void addInitialValues(Iterable<String> additionalValues) {
        if ( isEmpty(this.initialValues)) {
            setInitialValues(additionalValues);
        } else if ( additionalValues != null ) {
            addAll(this.initialValues, additionalValues.iterator());
        }
    }
    public Iterable<String> getInitialValues() {
        return initialValues;
    }
    /**
     * @see org.amplafi.flow.launcher.BaseFlowLauncher#getFlowState()
     */
    @Override
    protected <FS extends FlowState> FS getFlowState() {
        return null;
    }

    /**
     * @param propertyRoot
     * @param initialValues
     * @return
     */
    private Map<String, String> convertToMap() {
        Map<String, String> initialMap = new HashMap<String, String>();
        if ( initialValues != null) {

            for(String entry: initialValues) {
                String[] v = split(entry, "=", 2);
                String key = v[0];
                String lookup;
                if ( v.length < 2 ) {
                    lookup = key;
                } else {
                    lookup = v[1];
                }
                Object value = getValueFromBindingProvider() == null? lookup:getValueFromBindingProvider().getValueFromBinding(propertyRoot, lookup);
                initialMap.put(key, value == null?null:value.toString());
            }
        }
        return initialMap;
    }
    private ValueFromBindingProvider getValueFromBindingProvider() {
        return this.getFlowManagement().getValueFromBindingProvider();
    }
}
