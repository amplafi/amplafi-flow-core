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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public enum FlowLifecycleState {
    created,
    started,
    successful,
    canceled,
    failed, initializing;

    static {
        created.nextAllowed.add(canceled);
        created.nextAllowed.add(failed);
        created.nextAllowed.add(initializing);
        initializing.nextAllowed.add(started);
        initializing.nextAllowed.add(canceled);
        initializing.nextAllowed.add(failed);
        initializing.nextAllowed.add(successful);
        initializing.nextAllowed.add(started);
        started.nextAllowed.add(initializing);
        started.nextAllowed.add(canceled);
        started.nextAllowed.add(failed);
        started.nextAllowed.add(successful);
    }

    private final List<FlowLifecycleState> nextAllowed = new ArrayList<FlowLifecycleState>();
    public static void checkAllowed(FlowLifecycleState previousFlowLifecycleState,
            FlowLifecycleState nextFlowLifecycleState) {
        if (previousFlowLifecycleState != null &&
                previousFlowLifecycleState != nextFlowLifecycleState &&
                !previousFlowLifecycleState.nextAllowed.contains(nextFlowLifecycleState)) {
            throw new IllegalStateException("cannot go from "+
                                            previousFlowLifecycleState+" to "+nextFlowLifecycleState);
        }
    }
    /**
     * @return true if cannot transition out of this {@link FlowLifecycleState}.
     */
    public boolean isTerminatorState() {
        return this.nextAllowed.isEmpty();
    }
}
