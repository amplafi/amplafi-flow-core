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

/**
 * Used to indicate how should TransitionFlowActivity behave.
 *
 *
 */
public enum TransitionType {
    /**
     * when the current flow finishes then start a new flow of the indicated type.
     * this is the default transition.
     */
    normal(true),
    /**
     * when the current flow finishes using the alternate flow then start the transition flow.
     */
    alternate(true),
    /**
     * flow does not complete, but the flowstate is changed into a flow of the new type.
     * Useful to handle cases when the user is given the ability to select two different paths that are similar.
     * For example, a wizard that shows only "basic" flowactivities can be morphed into the advanced wizard.
     *
     * This avoids each flow having to do security checks or have complex decisions about which FlowActivity is displayed based on some boolean flag.
     */
    morphing(false),
    /**
     * This transition is canceling the current flow as part of the transition.
     *
     * Useful for the case when a cancel should not just be return to the previous state.
     */
    cancel(false),
    /**
     * do a call and return to the current flow
     * ? Before / After the current flow exists???
     */
    call_and_return(false);

    /**
     * The flow saveChanges() should be called and the flow exits normally.
     */
    private final boolean completesFlow;

    private TransitionType(boolean completesFlow)  {
        this.completesFlow = completesFlow;
    }

    /**
     * @return the completesFlow
     */
    public boolean isCompletesFlow() {
        return completesFlow;
    }
}
