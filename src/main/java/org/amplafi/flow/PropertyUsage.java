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
 * Describes how a property is to be used.
 * @author patmoore
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
