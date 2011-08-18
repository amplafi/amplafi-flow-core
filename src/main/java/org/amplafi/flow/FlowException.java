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
 * Base Exception for all flow issues.
 * @author patmoore
 *
 */
public class FlowException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final FlowState flowState;

    public FlowException() {
        flowState = null;
    }

    public FlowException(String message) {
        super(message);
        flowState = null;
    }
    public FlowException(FlowState flowState) {
        this.flowState = flowState;
    }

    /**
     * @return the flowState
     */
    public FlowState getFlowState() {
        return flowState;
    }
}
