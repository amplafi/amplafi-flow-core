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

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.*;

/**
 *
 */
public enum FlowLifecycleState {
    /**
     * FlowState created but nothing has happened to it.
     */
    created(false),
    /**
     * {@link FlowState#begin()} is in process.
     */
    initializing(false),
    /**
     * {@link FlowState#begin()} has successfully started the flow.
     */
    started(false),
    successful(true),
    canceled(true),
    failed(true);

    static {
        created.nextAllowed = Arrays.asList(canceled,failed,initializing);
        initializing.nextAllowed = Arrays.asList(started,canceled,failed,successful,started);
        started.nextAllowed = Arrays.asList(initializing,canceled,failed,successful);
    }

    private List<FlowLifecycleState> nextAllowed;
    private final boolean completed;

    private FlowLifecycleState(boolean completed) {
        this.completed = completed;
    }
    public static void checkAllowed(FlowLifecycleState previousFlowLifecycleState,
            FlowLifecycleState nextFlowLifecycleState) {
        if (previousFlowLifecycleState != null &&
                previousFlowLifecycleState != nextFlowLifecycleState &&
                (isEmpty(previousFlowLifecycleState.nextAllowed) || !previousFlowLifecycleState.nextAllowed.contains(nextFlowLifecycleState))) {
            throw new IllegalStateException("cannot go from "+
                                            previousFlowLifecycleState+" to "+nextFlowLifecycleState);
        }
    }
    /**
     * @return true if cannot transition out of this {@link FlowLifecycleState}.
     */
    public boolean isTerminatorState() {
        return isEmpty(this.nextAllowed);
    }
    /**
     * @return the completed
     */
    public boolean isCompleted() {
        return completed;
    }
}
