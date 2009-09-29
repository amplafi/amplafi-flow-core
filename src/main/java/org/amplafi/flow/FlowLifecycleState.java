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
     * {@link FlowState#initializeFlow()} is in progress.
     */
    initializing(true),
    /**
     *  {@link FlowState#initializeFlow()} has successfully completed.
     */
    initialized(true),
    /**
     * {@link FlowState#begin()} is in process.
     */
    starting(true),
    /**
     * {@link FlowState#begin()} has successfully started the flow.
     */
    started(true),
    successful(true),
    canceled(false),
    failed(false);

    static {
        created.nextAllowed = Arrays.asList(canceled,failed,initializing);
        initializing.nextAllowed = Arrays.asList(canceled,failed,successful,initialized);
        initialized.nextAllowed = Arrays.asList(canceled,failed,successful,starting);
        starting.nextAllowed = Arrays.asList(started,canceled,failed,successful,initializing,initialized);
        started.nextAllowed = Arrays.asList(canceled,failed,successful,initializing,initialized);
    }

    private List<FlowLifecycleState> nextAllowed;
    private final boolean verifyValues;

    private FlowLifecycleState(boolean verifyValues) {
        this.verifyValues = verifyValues;
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
     * @return true if this FlowLifecycleState permits verifying values.
     */
    public boolean isVerifyValues() {
        return verifyValues;
    }
}
