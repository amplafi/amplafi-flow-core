package org.amplafi.flow.launcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.amplafi.flow.Flow;
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
    private String flowTypeName;
    private String flowLabel;
    /**
     * Used for Listable items.
     */
    private Object keyExpression;
    private List<String> initialValues;
    private transient Object propertyRoot;

    public StartFromDefinitionFlowLauncher() {

    }
    public StartFromDefinitionFlowLauncher(String flowTypeName, FlowManagement flowManagement) {
        this(flowTypeName, new HashMap<String, String>(), flowManagement);
    }
    public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
            FlowManagement flowManagement) {
        this(flowTypeName, initialFlowState, flowManagement, flowTypeName);
    }

    public StartFromDefinitionFlowLauncher(String flowTypeName, Map<String, String> initialFlowState,
            FlowManagement flowManagement, Object keyExpression) {
        super(flowManagement, initialFlowState);
        this.flowTypeName = flowTypeName;
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
            FlowManagement flowManagement, Object keyExpression) {
        super(flowManagement, null);
        this.flowTypeName = flowTypeName;
        this.setInitialValues(initialValues);
        this.keyExpression = keyExpression;
        this.propertyRoot = propertyRoot;
    }

    @Override
    public FlowState call() {
        FlowState flowState;
        if(this.initialValues != null && !this.initialValues.isEmpty()) {
            flowState = getFlowManagement().startFlowState(getFlowTypeName(), true, propertyRoot, initialValues, getReturnToFlowLookupKey());
        } else {
            flowState = getFlowManagement().startFlowState(getFlowTypeName(), true, this.getValuesMap(), getReturnToFlowLookupKey());
        }
        return flowState;
    }

    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    @Override
    public String getFlowTypeName() {
        return flowTypeName;
    }
    public void setFlowLabel(String flowLabel) {
        this.flowLabel = flowLabel;
    }

    @Override
    public String getFlowLabel() {
        if ( flowLabel == null ) {
            Flow flow = getFlowManagement().getFlowDefinition(flowTypeName);
            flowLabel = flow.getLinkTitle();
        }
        return flowLabel;
    }

    public void setKeyExpression(Object keyExpression) {
        this.keyExpression = keyExpression;
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

    @Override
    public String toString() {
        return "StartFromDefinitionFlowLauncher :" +this.flowTypeName+ " initialValues="+getValuesMap();
    }
}
