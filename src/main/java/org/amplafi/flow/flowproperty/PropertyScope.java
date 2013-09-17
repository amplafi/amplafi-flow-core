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

import static org.amplafi.flow.flowproperty.PropertyUsage.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.sworddance.util.CUtilities.*;

/**
 * PropertyScope determines when a property is in scope (available) or out of scope ( can be gc'ed ) while the flow is active.
 * A completed flow has all properties gc'ed. Properties that need to be saved need to be copied to a subsequent flow.
 *
 * Property scope is used to determine namespace of property while the flow is active and the namespace list when
 * initializing the flow. While active the namespace used is the most narrow namespace.
 *
 * Once the flow completes, PropertyScope is used to determine where changes to the value should be copied.
 *
 * @author patmoore
 *
 */
public enum PropertyScope {
    /**
     * flowLocal to flow.
     * If {@link PropertyUsage} is NOT {@link PropertyUsage#initialize},
     * initial value is copied from global namespace.
     *
     * Need to make sure that callee Flows get their initial property settings from the caller flow.
     * Do we want flowLocal with inheritance?
     *
     * On flow success, changes are copied back to global namespace if {@link PropertyUsage} is {@link PropertyUsage#io}
     *
     * TODO put in a flowState.lookupKey namespace
     */
    flowLocal(use),
    /**
     * If {@link PropertyUsage} is NOT {@link PropertyUsage#initialize},
     * Initial value is copied from flow namespace or no value in flow namespace then the global namespace.
     *
     * Changes are copied back to global namespace if {@link PropertyUsage} is {@link PropertyUsage#io}
     *
     * This allows a FA to have a private namespace so it can save info knowing
     * that it will not impact another FA.
     */
    activityLocal(use),
    /**
     * The property is not saved into the flowState map. It is available for the current request only
     * and is cleared when current transaction completes. It has the same scope as flowLocal
     *
     * I am not a big fan of 'flash' persistence which clears a value on the completion of the next transaction. Typically used for telling user
     * that the requested action was completed.
     *
     * Useful for passing values between different FAs while doing things like saveChanges()
     *
     * Reasons I dislike 'flash':
     * <ul><li>If user clicks another action immediately, they see the message.</li>
     * <li>If user clicks another action, sees the 'flash' message pop-up. There is no 'go back' support for seeing the flash message.</li>
     * <li>A history tracker is better</li>
     * </ul>
     *
     * Note: TODO: currently, a requestFlowLocal property must not have a {@link org.amplafi.flow.FlowActivityPhase} requirement because the
     * {@link org.amplafi.flow.FlowActivityPhase} requirement is done by checking to see if the property is set. We cannot just check to see if there is a {@link org.amplafi.flow.FlowPropertyValueProvider}
     * because a {@link org.amplafi.flow.FlowPropertyValueProvider} may not actually set a value.
     */
    requestFlowLocal(true, io, true),
    /**
     * A global property represents a property that:
     * 1) the current flow has no knowledge of the data ( passthrough case )
     * 2) OR can be changed by the flow in a publicly visible way even if the flow fails.
     * ( see {@link #flowLocal} )
     *
     * Use cases:
     * 1) a value that is set by called subflow that will be made available to subsequent flows.
     * 2) a flow may be morphed into another flow and that value is meaningful to the other flow.
     * 3) any alteration of a global is in the un-namespaced part of the FlowState map, therefore PropertyUsage that depends on a
     * flow local copy of the property does not apply: ( {@link PropertyUsage#internalState}, {@link PropertyUsage#use}, {@link PropertyUsage#consume} )
     * (special note on {@link PropertyUsage#consume}: consume does its clearing when the flow is initialized - so the value is lost no matter how the flow
     * exits. This means that global/consumes is not an option because the global/consumes value will not be available for the flow itself. )
     *
     * Since the flow does not understand the property, it shouldn't try to manage it. However, this does leave the question about
     * how to clean out these orphaned values.
     *
     * Therefore, global is deprecated to see if we can avoid using it.
     *
     * EXPERIMENTAL -- does global mean that any changes the flow makes are supposed to be 'universally visible'/instantly visible outside of flow?
     * How is this different than saveImmediate? Why not just have flowLocal with PropertyUsage be {@link PropertyUsage#io}?
     *
     * May be null namespace is just the 'transfer' namespace?
     * There should NEVER be a universal flow state because:
     * <ul><li>Security hole: user could change which BP is active. They start an admin flow within a BP that they are admin on.
     * <li>change their active BP to one in which they are much lower</li>
     * <li>with global flow states this could change the BP of the admin flow -- constant battle to make sure that no security hole opens.</li>
     * </ul>
     * Notes on excluded
     */
    @Deprecated
    global(false, io, false, new PropertyUsage[] { suppliesIfMissing, initialize, io })
    ;
    private final boolean localToFlow;
    private final PropertyUsage defaultPropertyUsage;
    private final Set<PropertyUsage> allowPropertyUsages;
    /**
     * Property should not be persisted as string in the FlowState map. This is
     * useful caching values during this transaction. TODO maybe allow
     * non-entities that are Serializable to last beyond current transaction?
     *
     * modifications to this property are not persisted, but are cached.
     *
     * if its sensitive we have already forced this to cache only.
     * this prevents passwords from being saved into the flowstate db table.
     */
    private final boolean cacheOnly;
    private PropertyScope(PropertyUsage defaultPropertyUsage) {
        this(true, defaultPropertyUsage, false, PropertyUsage.values());
    }
    private PropertyScope(boolean localToFlow, PropertyUsage defaultPropertyUsage, boolean cacheOnly, PropertyUsage... allowedPropertyUsages) {
        this.localToFlow = localToFlow;
        this.defaultPropertyUsage = defaultPropertyUsage;
        this.cacheOnly = cacheOnly;
        this.allowPropertyUsages = new HashSet<PropertyUsage>();
        if (isNotEmpty(allowedPropertyUsages)) {
            this.getAllowPropertyUsages().addAll(Arrays.asList(allowedPropertyUsages));
        }
        if ( this.defaultPropertyUsage != null ) {
            this.getAllowPropertyUsages().add(defaultPropertyUsage);
        }
    }
    /**
     * @return the namespacedProperty
     */
    public boolean isLocalToFlow() {
        return localToFlow;
    }
    /**
     * @return the defaultPropertyUsage
     */
    @Deprecated // use FlowPropertyDefinitionBuilder to get the property explicitly set.
    public PropertyUsage getDefaultPropertyUsage() {
        return defaultPropertyUsage;
    }
    /**
     * @return the cacheOnly
     */
    public boolean isCacheOnly() {
        return cacheOnly;
    }
    /**
     * @return the allowPropertyUsages
     */
    public Set<PropertyUsage> getAllowPropertyUsages() {
        return allowPropertyUsages;
    }
    public boolean isAllowedPropertyUsage(PropertyUsage propertyUsage) {
        return allowPropertyUsages.contains(propertyUsage);
    }
}
