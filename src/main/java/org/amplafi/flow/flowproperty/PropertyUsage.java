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
 * PropertyUsage is *NOT* a security mechanism. PropertyUsage focuses only on how a property should be initialized and outputed so that
 * a property lifecycle can be determined.
 *
 * TODO: rename to PropertyIO
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
     * No {@link org.amplafi.flow.FlowPropertyValueProvider} are allowed
     */
    use(false, false, true),
    /**
     * Similar to {@link #use} but any changes to the property will be copied back to the final result.
     * parameter is read if passed. No guarantee that the property will have a value upon flow completion.
     */
    io(false, true, true, null),
    /**
     * Like {@link #io} except the property will be assigned a value if there is none and the created value will be visible externally.
     * The object in question must exist already.
     *
     * The value returned must be a derived value.
     *
     */
    suppliesIfMissing(false, true, true),
    /**
     * EXPERIMENTAL -- do not use yet.
     * Similar to {@link #suppliesIfMissing} except that the property may be created.
     * Use case: edit or create flows where the flow is modifying an existing object or creating an new object.
     *
     * A {@link FlowPropertyValuePersister} must be attached to the property definition.
     */
    createsIfMissing(false, true, true),
    /**
     * Like {@link #use} except the value will not be visible to any later flows.
     * the property can be initialized externally ( using the global namespace - null ). The initialization value will be moved into the flow/activity namespace
     * Any changes the flow makes are not visible after flow completes.
     * altersProperty = true because it sets value to null!
     *
     * Note: consume will set a NULL when completing the flow, so it sets a value but not the value in the flowstate.
     */
    consume(true, false, true, Boolean.TRUE),

    /**
     * Like {@link #suppliesIfMissing} except that any previous value will be cleared AND ignored.
     *
     * In general limit the usage of {@link #initialize} to those case where a fresh value is mandatory.
     * Consider if {@link #suppliesIfMissing} is adequate as {@link #initialize} forces a database access or other expensive initialization
     * and prevents the flow from being passed the property value by a caller flow.
     *
     * If the property has a FlowPropertyValueProvider, the FlowPropertyValueProvider will be called on {@link org.amplafi.flow.impl.FlowStateImplementor#initializeFlow()}.
     *
     * TODO: does this mean eager initialization ( to allow for triggering of activities that take a while to initialize? ) OR do is a new propertyUsage better?
     * TODO: PropertyRequired setting will be used to determine when the initialization will be forced to happen. ( if it hasn't happened already )
     * Use case:
     * setting the user id and broadcastProvider for a flow
     *
     * If the property returns a database object, the object must already exist. If the property returned is a derived property or a value that is stored
     * as part of another database object then {@link #initialize} should be used.
     *
     * If the property's object is a database object then {@link #createAlways} must be used.
     *
     * For example, FIRST_NAME would be {@link #initialize} but never {@link #createAlways} because FIRST_NAME is a not a database object.
     */
    initialize(true, true, false),
    /**
     * EXPERIMENTAL -- do not use yet.
     *
     * Like {@link #initialize} except that the object is always created ( object must be a database object )
     *
     * In general limit the usage of {@link #createAlways} to those case where a fresh value is mandatory.
     * Consider if {@link #createsIfMissing} is adequate as {@link #createAlways} prevents the flow from being a
     * edit/or create flow.
     *
     * A {@link FlowPropertyValuePersister} must be attached to the property definition.
     */
    createAlways(true, true, false);

    /**
     * clear from global namespaces when copied to local flow state's namespace. Namespaces in question are determined by PropertyScope??
     */
    private final boolean cleanOnInitialization;
    private final boolean outputedProperty;
    private final boolean externallySettable;
    /**
     * if false then it is known that this property WILL NOT set a value in the export map.
     * if true then it is known that this property WILL set a value on the export map ( but maybe null ).
     * if null then it is unknown if this property will be changed. (for example, #io )
     */
    private final Boolean altersProperty;
    private List<PropertyUsage> canBeChangedTo;

    static {
        // roughly trying to enforce that can become more strict but not less strict.
        // notice that this some changes take away behavior ( for example, use -> consume removes the property )
        internalState.canBeChangedTo = Arrays.asList();
        use.canBeChangedTo = Arrays.asList(io, suppliesIfMissing, consume, initialize);
        io.canBeChangedTo = Arrays.asList(suppliesIfMissing, consume, initialize);
        suppliesIfMissing.canBeChangedTo = Arrays.asList(initialize);
        createsIfMissing.canBeChangedTo = Arrays.asList(initialize, suppliesIfMissing);
        consume.canBeChangedTo = Arrays.asList();
        initialize.canBeChangedTo = Arrays.asList();
        createAlways.canBeChangedTo = Arrays.asList();
    }

    public static List<PropertyUsage> NO_FLOW_PROPERTY_VALUE_PROVIDERS = Arrays.asList(use, consume);

    private PropertyUsage(boolean cleanOnInitialization, boolean outputedProperty, boolean externallySettable) {
        this(cleanOnInitialization,outputedProperty, externallySettable, outputedProperty);
    }
    private PropertyUsage(boolean cleanOnInitialization, boolean outputedProperty, boolean externallySettable, Boolean altersProperty) {
        this.cleanOnInitialization = cleanOnInitialization;
        this.outputedProperty = outputedProperty;
        this.externallySettable = externallySettable;
        this.altersProperty = altersProperty;
    }

    public boolean isCleanOnInitialization() {
        return this.cleanOnInitialization;
    }


    /**
     * Intent is that we can determine if a property is set. Useful to see if 1 flow can start another flow that has
     * property that must be set before starting the flow (  {@link FlowActivityPhase#activate} )
     * @see #getAltersProperty()
     * @return
     */
    public boolean isOutputedProperty() {
        return this.outputedProperty;
    }

    /**
     * True means the property can be set externally.
     *
     * This is *NOT* a security mechanism. See {@link ExternalPropertyAccessRestriction} for security
     * @return true means the property can be initialized externally.
     * false means the property is always set by the flow itself.
     */
    public boolean isExternallySettable() {
        return this.externallySettable;
    }

    public boolean isChangeableTo(PropertyUsage other) {
        return other==this || (other != null && this.canBeChangedTo.contains(other));
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
            return other.canBeChangedTo.size() > current.canBeChangedTo.size()?current:other;
        }
    }

    /**
     * This differs from {@link #isOutputedProperty()} because the property previous value may be altered to null ( i.e. the property is unset ).
     * This is the case for {@link #consume} which sets the property to null but does not output a value.
     *
     * For case of {@link #io}, the property may or may not change ( not known for certain.)
     *
     * @return null if not known if property is altered. Assume to be true when determining if property is readonly. Return true if property will for certain be altered.
     */
    public Boolean getAltersProperty() {
        return this.altersProperty;
    }
}
