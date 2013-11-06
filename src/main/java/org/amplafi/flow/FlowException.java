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

import static com.sworddance.util.ApplicationIllegalStateException.checkState;
import static org.apache.commons.lang.StringUtils.join;
/**
 * For example, missing data.
 *
 * @author patmoore
 *
 */
public class FlowException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private FlowState flowState;

    public FlowException(FlowState flowState) {
    	this.flowState = flowState;
    }

    public FlowException(Exception exception) {
        super(exception);
    }
    public FlowException(String message, Exception exception) {
        super(message, exception);
    }
    public FlowException(Object... messages) {
        super(join(messages));
    }
    public FlowException(FlowState flowState, Object... messages) {
        this(messages);
        this.flowState = flowState;
    }

    /**
     * @return the flowState
     */
    public FlowState getFlowState() {
        return flowState;
    }

	/**
	 * Only allowed to be called when a flow state wasn't yet set.
	 *
	 * @param flowState
	 */
	public void setFlowState(FlowState flowState) {
		checkState(!isFlowStateSet(), "Trying to override non-null flowState");
		this.flowState = flowState;
	}

	public boolean isFlowStateSet() {
		return flowState != null;
	}

	public static FlowException notNull(Object notNull, FlowState flowState, Object...messages) {
	    if (notNull == null) {
	        throw new FlowException(flowState, join(messages));
	    }
	    return null;
	}
}
