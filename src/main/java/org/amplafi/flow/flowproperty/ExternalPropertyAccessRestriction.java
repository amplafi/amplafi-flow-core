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

import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowState;

/**
 * Controls GLOBAL external access and visibility to the property from an untrusted source (like a browser or plugin).
 * Defines the external visibility but does NOT impact the ability of other flows' access.
 *
 * ExternalPropertyAccessRestriction determines if setProperty() or getProperty() are allowed via external clients.
 *
 * In order for ExternalPropertyAccessRestriction to be checked properly, external client property changes should have a different
 * entry point than internal changes.
 *
 * ExternalPropertyAccessRestriction does NOT affect {@link FlowPropertyValueProvider} setting a value
 * This differs from how the property is initialized and exported ( {@link PropertyUsage} ) and how
 * broad any changes to the property are spread. ( PropertyScope )
 * but this may overlap with {@link PropertyUsage#isExternallySettable()}
 *
 * ========================================================================================
 * Additionally clarification about difference between PropertyUsage and ExternalPropertyAccessRestriction:
 *
 * Example #1:
 *
 * a property may be allowed to be set via another flow ( PropertyUsage.io )
 * But the property value cannot be shared with an external client ( {@link ExternalPropertyAccessRestriction#noAccess})
 *
 * Example #2:
 * {@link ExternalPropertyAccessRestriction#readonly} :
 * a property may be only used (but not set by a flow). This does NOT mean that the property is not externally settable.
 * For example, most security parameters or parameters that come from the environment (i.e. http session)
 * =======================================================================================
 *
 * TODO: what about security levels based on if the data is provided by a potentially tainted source (clients) vs. another flow?
 * so "external" means another flow and "client" means to a browser client or api client.
 *
 * @author patmoore
 *
 */
public enum ExternalPropertyAccessRestriction {
    /**
     * no access. These are properties that are really internal state only.
     * The value is usually a configuration property.
     *
     * TODO: This should be the default so that explicit exposure is required
     *
     * Property must NOT be included in auto generated documentation.
     * TODO maybe an explicit configOnly enum?
     * TODO need method to allow setting secured parameters?
     */
    noAccess(false, false),
    /**
     * property value is visible external to the user. property value is NOT modifiable by <em>external</em> sources even during the Flow.
     * The property may be changed, set or deleted by the flow itself.
     * The change prohibition during the flow
     * makes this different than {@link PropertyUsage#use}
     */
    readonly(true, false),
    /**
     * property can be altered but the original value and the alteration is not visible to <em>external</em> sources. The property value can
     * be made available
     * this is typically used for passwords (or any other security credential )
     * User is allowed to send in the password but not allowed to see stored passwords.
     *
     * This does not affect internal flow code which is always permitted get/set access.
     */
    writeonly(false, true),
    /**
     * can be accessed externally both for modification, viewing
     */
    noRestrictions(true, true);

    private final boolean externalReadAccessAllowed;
    private final boolean externalWriteAccessAllowed;
    /**
     * @param externalReadAccess
     * @param externalWriteAccess
     */
    private ExternalPropertyAccessRestriction(boolean externalReadAccess, boolean externalWriteAccess) {
        this.externalReadAccessAllowed = externalReadAccess;
        this.externalWriteAccessAllowed = externalWriteAccess;
    }
    /**
     * @return if false, {@link FlowState#getProperty(String)} method should return null rather than return a result.
     *
     */
    public boolean isExternalReadAccessAllowed() {
        return externalReadAccessAllowed;
    }
    /**
     * Should security check happen at different point?
     * @return if false, {@link FlowState#setProperty(String, Object)} will be ignored. If true and {@link #isExternalReadAccessAllowed()} is false then
     * care must be taken to not accidently
     */
    public boolean isExternalWriteAccessAllowed() {
        return externalWriteAccessAllowed;
    }
    public boolean isDocumented() {
        return isExternalReadAccessAllowed() || isExternalWriteAccessAllowed();
    }
}
