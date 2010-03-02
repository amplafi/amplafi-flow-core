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
import java.util.List;
import java.util.Map;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.apache.commons.lang.ObjectUtils;


import static org.apache.commons.collections.CollectionUtils.*;

/**
 * {@link FlowLauncher} that starts a new {@link FlowState}.
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
        this.keyExpression = keyExpression;
    }
    public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState, Serializable keyExpression) {
        super(flowTypeName, initialFlowState, keyExpression);
        this.keyExpression = keyExpression;
    }
    /**
    *
    * @param flowTypeName
    * @param initialValues used to define the initial values for flow. This is a
    * list of strings. Each string is 'key=value'. if value is the same name as a component
    * that has a 'value' attribute (like TextField components) then the initial value.
    * If value is a container's property then that value is used. Otherwise the value
    * provided is used as a literal.
    * @param propertyRoot
    * @param flowManagement
    * @param keyExpression for html rendering identification.
    */
    public StartFromDefinitionFlowLauncher(String flowTypeName, Object propertyRoot, Iterable<String> initialValues,
            FlowManagement flowManagement, Serializable keyExpression) {
        super(flowTypeName, flowManagement, null, keyExpression);
        this.setInitialValues(initialValues);
        this.keyExpression = keyExpression;
        this.propertyRoot = propertyRoot;
    }

    @Override
    public FlowState call() {
        FlowState flowState;
        if(this.initialValues != null && !this.initialValues.isEmpty()) {
            flowState = getFlowManagement().startFlowState(getFlowTypeName(), true, propertyRoot, initialValues, getReturnToFlow());
        } else {
            flowState = getFlowManagement().startFlowState(getFlowTypeName(), true, this.getValuesMap(), getReturnToFlow());
        }
        return flowState;
    }

    @Deprecated // used only one place and that is questionable.
    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    public Object getKeyExpression() {
        return keyExpression;
    }

    public boolean hasKey(Object key) {
        return ObjectUtils.equals(this.keyExpression, key);
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
    protected FlowState getFlowState() {
        return null;
    }


}
