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
package org.amplafi.flow.impl;

import java.util.Map;

import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowPropertyDefinition;
import org.amplafi.flow.FlowPropertyValueProvider;
import org.amplafi.flow.FlowStepDirection;
import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.amplafi.flow.validation.FlowValidationResult;
import org.amplafi.flow.validation.FlowValidationResultProvider;
import org.amplafi.flow.validation.MissingRequiredTracking;
import org.amplafi.flow.validation.ReportAllValidationResult;

import com.sworddance.util.ApplicationIllegalArgumentException;

import static com.sworddance.util.CUtilities.*;

/**
 * This service validates that all the required properties have a value.
 *
 * NOTE: This check has been tightened up. It used to be enough that the flowPropertyDefinition had a {@link FlowPropertyValueProvider}.
 * However, this is not adequate for many cases, in particular security properties. It is not enough that a user property have a
 * provider if the security check fails then the User property will not have a value and allowing the validation check should not pass.
 *
 * TODO: should be singleton service?
 * @author patmoore
 *
 */
public class FlowValidationResultProviderImpl<FPP extends FlowPropertyProviderWithValues> implements FlowValidationResultProvider<FPP> {

    // TODO: should be singleton service?
    public static final FlowValidationResultProviderImpl<FlowPropertyProviderWithValues> INSTANCE = new FlowValidationResultProviderImpl<FlowPropertyProviderWithValues>();
    /**
     * @see org.amplafi.flow.validation.FlowValidationResultProvider#getFlowValidationResult(org.amplafi.flow.flowproperty.FlowPropertyProvider, org.amplafi.flow.FlowActivityPhase, org.amplafi.flow.FlowStepDirection)
     */
    @Override
    public FlowValidationResult getFlowValidationResult(FPP flowPropertyProvider, FlowActivityPhase flowActivityPhase,
        FlowStepDirection flowStepDirection) {
        // TODO : Don't validate if user is going backwards.
        // Need to handle case where user enters invalid data, backs up and then tries to complete the flow
        FlowValidationResult result = new ReportAllValidationResult();
        ApplicationIllegalArgumentException.notNull(flowActivityPhase, "FlowActivityPhase must be set.");
        Map<String, FlowPropertyDefinition> propDefs = flowPropertyProvider.getPropertyDefinitions();
        if (isNotEmpty(propDefs)) {
            for (FlowPropertyDefinition def : propDefs.values()) {
                FlowActivityPhase propertyRequired = def.getPropertyRequired();
				if (propertyRequired != null && propertyRequired == flowActivityPhase) {
                    if (!flowPropertyProvider.isPropertySet(def.getName())) {
                        // the property is not set
                        // It does make sense for PropertyUsage.consumes.getSetsValue() == Boolean.TRUE because this indicates
                        // that if the property is required, then the property must be available to be consumed at the specified point.
                        if ( def.isAutoCreate()) {
                            // this property is expected to be created at this point. see if we can force the value to be created
                        	// Note: This actually enforces the expectation that the property can in fact be supplied.
                            flowPropertyProvider.getProperty(def.getName());
                        }
                        MissingRequiredTracking.appendRequiredTrackingIfTrue(result,
                                !flowPropertyProvider.isPropertySet(def.getName()),
                                def.getUiComponentParameterName());
                    }
                }
            }
        }
        return result;
    }

}
