/**
 * Copyright 2006-2008 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow;

/**
 * @author patmoore
 *
 */
public enum PropertyUsage {
    /**
     * a parameter that if provided will be cleared on exit.
     */
    consume(true),
    /**
     * read if passed. may generate (?guaranteed to exist after this flow?)
     */
    io(false),
    /**
     * read if passed. not created.
     */
    use(false),
    /**
     * flowLocal to flow. Can not be set from outside flow.
     * TODO put in a flowState.lookupKey namespace
     */
    flowLocal(true),
    /**
     * This allows a FA to have a private namespace so it can save info knowing
     * that it will not impact another FA.
     */
    activityLocal(true),
    /**
     * a temporary property usage.
     */
    other(false),
    ;

    private final boolean clearOnExit;

    private PropertyUsage(boolean clearOnExit) {
        this.clearOnExit = clearOnExit;
    }

    /**
     * @return the clearOnExit
     */
    public boolean isClearOnExit() {
        return clearOnExit;
    }
}
