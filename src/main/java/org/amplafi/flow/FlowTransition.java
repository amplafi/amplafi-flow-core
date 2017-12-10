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
package org.amplafi.flow;

import java.util.Map;

import org.amplafi.flow.launcher.ContinueFlowLauncher;
import org.amplafi.flow.launcher.FlowLauncher;
import org.amplafi.flow.launcher.MorphFlowLauncher;
import org.amplafi.flow.launcher.StartFromDefinitionFlowLauncher;
import org.amplafi.flow.translator.SerializationWriter;

import com.sworddance.util.map.MapKeyed;


import static org.apache.commons.lang.StringUtils.*;
/**
 * describes how to transition from one flow to another.
 *
 * TODO: undo transition ability
 * @author patmoore
 *
 */
// TODO : remove JsonSelfRenderer implementation
public class FlowTransition implements FlowSelfRenderer<FlowTransition>, MapKeyed<String> {

    private static final String INITIAL_VALUES = "initialValues";

    private static final String TRANSITION_TYPE = "transitionType";

    private static final String NEXT_FLOW = "nextFlow";
    private static final String NEXT_FLOW_TYPE = "nextFlowType";

    private static final String LABEL = "label";
    private static final String KEY = "key";
    private static final String TRANSITION_COMPLETION_MESSAGE = "transitionCompletionMessage";

    private String key;

    private String label;
    private String transitionCompletionMessage;

    private String nextFlow;
    private String nextFlowType;

    private TransitionType transitionType;

    private Map<String, String> initialValues;

    public FlowTransition() {
        // only for hibernate.
    }
    public FlowTransition(String nextFlow, String label, Map<String, String> initialValues) {
        this(nextFlow, nextFlow, label, TransitionType.alternate, initialValues);
    }
    /**
     * the transition key will be nextFlow
     * @param nextFlowType the type of the next flow to be executed.
     * @param label
     * @param transitionType
     * @param initialValues
     */
    public FlowTransition(String nextFlowType, String label, TransitionType transitionType, Map<String, String> initialValues) {
        this(nextFlowType, nextFlowType, label, transitionType, initialValues);
    }
    public FlowTransition(String key, String nextFlowType, String label, TransitionType transitionType, Map<String, String> initialValues) {
        this.key = isBlank(key)?transitionType.toString():key;
        this.label = label;
        this.nextFlowType = nextFlowType;
        this.setTransitionType(transitionType);
        this.initialValues = initialValues;
    }

    public FlowTransition(JSONObject jsonObject) {
        fromJson(jsonObject);
    }

    public String getNextFlow() {
        return nextFlow;
    }

    public void setNextFlow(String nextFlow) {
        this.nextFlow = nextFlow;
    }

    public String getNextFlowType() {
        return nextFlowType;
    }

    public void setNextFlowType(String nextFlowType) {
        this.nextFlowType = nextFlowType;
    }
    /**
     *
     * @return the label key to attach to a link or button that will trigger this transition.
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getInitialValues() {
        return initialValues;
    }

    public void setInitialValues(Map<String, String> initialValues) {
        this.initialValues = initialValues;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T fromSerialization(Object object) {
        JSONObject json = JSONObject.toJsonObject(object);
        this.key = json.optString(KEY);
        this.label = json.optString(LABEL);
        this.nextFlow = json.optString(NEXT_FLOW);
        this.nextFlowType = json.optString(NEXT_FLOW_TYPE);
        this.transitionType = (TransitionType) EnumFlowRenderer.INSTANCE.fromJson(TransitionType.class, json.opt(TRANSITION_TYPE));
        this.transitionCompletionMessage = json.optString(TRANSITION_COMPLETION_MESSAGE);
        this.initialValues = MapJsonRenderer.INSTANCE.fromJson(Map.class, json.opt(INITIAL_VALUES));
        return (T) this;
    }

    @Override
    public <W extends SerializationWriter> W toSerialization(W jsonWriter) {
        jsonWriter.object();
        jsonWriter.keyValueIfNotNullValue(KEY, getMapKey());
        jsonWriter.keyValueIfNotNullValue(LABEL, getLabel());
        jsonWriter.keyValueIfNotNullValue(NEXT_FLOW, getNextFlow());
        jsonWriter.keyValueIfNotNullValue(NEXT_FLOW_TYPE, getNextFlowType());
        jsonWriter.keyValue(TRANSITION_TYPE, transitionType);
        jsonWriter.keyValue(INITIAL_VALUES, getInitialValues());
        jsonWriter.keyValue(TRANSITION_COMPLETION_MESSAGE, this.getTransitionCompletionMessage());
        jsonWriter.endObject();
        return jsonWriter;

    }

    /**
     * @see com.sworddance.util.map.MapKeyed#getMapKey()
     */
    @Override
    public String getMapKey() {
        return key;
    }
    /**
     * @param transitionType the transitionType to set
     */
    public void setTransitionType(TransitionType transitionType) {
        this.transitionType = transitionType;
    }
    /**
     * @return the transitionType
     */
    public TransitionType getTransitionType() {
        return transitionType;
    }

    public FlowLauncher getFlowLauncher(FlowState flowState) {
        FlowActivityImplementor flowActivity = flowState.getCurrentActivity();
        FlowLauncher flowLauncher = null;
        String resolvedNextFlow = flowActivity.resolveIndirectReference(getNextFlow());
        String resolvedNextFlowType = flowActivity.resolveIndirectReference(getNextFlowType());
        if ( isMorphingFlow()) {
            // HACK: Do not understand reason why an existing flow would morph to another existing flow
            // which is what passing resolvedNextFlow seems to imply.
            // or maybe this is to continue the chain of Flows ?? needs investigation
            flowLauncher = new MorphFlowLauncher(resolvedNextFlowType, resolvedNextFlow, flowState.getFlowManagement());
        } else if ( resolvedNextFlow != null) {
            FlowState resolvedNextFlowState = flowState.getFlowManagement().getFlowState(resolvedNextFlow);
            flowLauncher = new ContinueFlowLauncher(resolvedNextFlowState, flowState.getFlowManagement());
        } else if ( resolvedNextFlowType != null) {
            flowLauncher = new StartFromDefinitionFlowLauncher(resolvedNextFlowType, flowState.getFlowManagement());
        }
        return flowLauncher;
    }
    /**
     * @return true if causes flow to morph
     */
    public boolean isMorphingFlow() {
        return this.transitionType == TransitionType.morphing;
    }
    public boolean isCompletingFlow() {
        return this.transitionType != null && this.transitionType.isCompletesFlow();
    }

    /**
     * @param transitionCompletionMessage the transitionCompletionMessage to set
     */
    public void setTransitionCompletionMessage(String transitionCompletionMessage) {
        this.transitionCompletionMessage = transitionCompletionMessage;
    }
    /**
     * @return message used to announce a successful completion of the transition.
     */
    public String getTransitionCompletionMessage() {
        return transitionCompletionMessage;
    }

    @Override
    public String toString() {
        JSONStringer writer = new JSONStringer();
        writer.value(this);
        return writer.toString();
    }
}
