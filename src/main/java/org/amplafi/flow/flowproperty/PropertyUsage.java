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

package org.amplafi.flow.flowproperty;

import java.util.Arrays;
import java.util.List;

import org.amplafi.flow.FlowActivityPhase;

/**
 * Describes how a property may be initialized and how visible the property changes are external to flow after the flow terminates.
 * While a flow is in progress no changes are visible to other flows ( except those invoked in a caller/callee relationship )
 * 
 * TODO: How to handle caller/callee situations? Seems like internalState should be made available.
 * 
 *
 * @author patmoore
 */
public enum PropertyUsage {
    /**
     * ignores outside namespaces. Does not initialize from them nor does is property copied back to the outside namespaces.
     *
     * These properties are considered invisible properties that should not be documented to external users.
     * 
     * Because this property does not look at the externally set values, the state of a property with the same name
     * as this internal state property is not affected.
     *
     */
    internalState(false, false, false),
    /**
     * read if passed. not created. The original value is not altered even if the flow itself does alter the value.
     */
    use(false, false, true),
    /**
     * Similar to {@link #use} but any changes to the property will be copied back to the final result.
     * parameter is read if passed. No guarantee that the property will have a value upon flow completion.
     */
    io(false, true, true, null),
    /**
     * Like {@link #io} except the property will be assigned a value if there is none and the created value will be visible externally.
     * This is enforced 
     *
     */
    suppliesIfMissing(false, true, true),
    /**
     * Like {@link #use} except the value will not be visible to any later flows.
     * the property can be initialized externally ( using the global namespace - null ). The initialization value will be moved into the flow/activity namespace
     * Any changes the flow makes are not visible after flow completes.
     * setsValue = true because it sets value to null!
     * 
     * Note: consume will set a NULL when completing the flow, so it sets a value but not the value in the flowstate.
     */
    consume(true, false, true, Boolean.TRUE),

    /**
     * Like {@link #suppliesIfMissing} except that any previous value will be cleared AND ignored.
     * If the property has a FlowPropertyValueProvider, the FlowPropertyValueProvider will be called on {@link org.amplafi.flow.impl.FlowStateImplementor#initializeFlow()}.
     *
     * TODO: does this mean eager initialization ( to allow for triggering of activities that take a while to initialize? ) OR do is a new propertyUsage better?
     * TODO: PropertyRequired setting will be used to determine when the initialization will be forced to happen. ( if it hasn't happened already )
     * Use case:
     * setting the user id and broadcastProvider for a flow
     */
    initialize(true, true, false);

    /**
     * clear from global namespaces when copied to local flow state's namespace. Namespaces in question are determined by PropertyScope??
     */
    private final boolean cleanOnInitialization;
    private final boolean copyBackOnFlowSuccess;
    private final boolean externallySettable;
    /**
     * if false then it is known that this property WILL NOT set a value in the export map.
     * if true then it is known that this property WILL set a value on the export map ( but maybe null ).
     * if null then it is unknown if this property will be changed. (for example, #io )
     */
    private final Boolean setsValue;
    private List<PropertyUsage> canbeChangedTo;
    
    static {
        // roughly trying to enforce that can become more strict but not less strict.
        // notice that this some changes take away behavior ( for example, use -> consume removes the property )
        internalState.canbeChangedTo = Arrays.asList();
        use.canbeChangedTo = Arrays.asList(io, suppliesIfMissing, consume, initialize);
        io.canbeChangedTo = Arrays.asList(suppliesIfMissing, consume, initialize);
        suppliesIfMissing.canbeChangedTo = Arrays.asList(initialize);
        consume.canbeChangedTo = Arrays.asList();
        initialize.canbeChangedTo = Arrays.asList();
    }

    private PropertyUsage(boolean cleanOnInitialization, boolean copyBackOnFlowSuccess, boolean externallySettable) {
        this(cleanOnInitialization,copyBackOnFlowSuccess, externallySettable, copyBackOnFlowSuccess);
    }
    private PropertyUsage(boolean cleanOnInitialization, boolean copyBackOnFlowSuccess, boolean externallySettable, Boolean setsValue) {
        this.cleanOnInitialization = cleanOnInitialization;
        this.copyBackOnFlowSuccess = copyBackOnFlowSuccess;
        this.externallySettable = externallySettable;
        this.setsValue = setsValue;
    }

    public boolean isCleanOnInitialization() {
        return cleanOnInitialization;
    }

    /**
     * @return the copyBackOnExit
     */
    public boolean isCopyBackOnFlowSuccess() {
        return copyBackOnFlowSuccess;
    }

    /**
     * True means the property can be set externally. 
     * TODO: {@link PropertySecurity} should be used?
     * @return true means the property can be initialized externally by previous flows or from clients.
     * false means the property is always set by the flow itself. ( Use case: security parameters ) 
     * 
     */
    public boolean isExternallySettable() {
        return externallySettable;
    }

    public boolean isChangeableTo(PropertyUsage other) {
        return other==this || (other != null && canbeChangedTo.contains(other));
    }

    public static PropertyUsage survivingPropertyUsage(PropertyUsage current, PropertyUsage other) {
        if ( other == null || other == current) {
            return current;
        } else if ( current == null ) {
            return other;
        } else if ( !other.isChangeableTo(current)) {
            if ( current.isChangeableTo(other)) {
                return other;
            } else {
                // no change
                return current;
            }
        } else if (!current.isChangeableTo(other)) {
            // other can change to current but current can not change to other
            return current;
        } else {
            // both can change to each other - return most restrictive
            return other.canbeChangedTo.size() > current.canbeChangedTo.size()?current:other;
        }
    }

    /**
     * Intent is that we can determine if a property is set. Useful to see if 1 flow can start another flow that has
     * property that must be set before starting the flow (  {@link FlowActivityPhase#activate} )
     * @return
     */
    public Boolean getSetsValue() {
        return setsValue;
    }

}
