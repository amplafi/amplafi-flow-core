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
