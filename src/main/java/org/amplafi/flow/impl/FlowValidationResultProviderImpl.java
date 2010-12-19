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
import org.amplafi.flow.FlowStepDirection;
import org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues;
import org.amplafi.flow.validation.FlowValidationResult;
import org.amplafi.flow.validation.FlowValidationResultProvider;
import org.amplafi.flow.validation.MissingRequiredTracking;
import org.amplafi.flow.validation.ReportAllValidationResult;

import static com.sworddance.util.CUtilities.*;

/**
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
        Map<String, FlowPropertyDefinition> propDefs = flowPropertyProvider.getPropertyDefinitions();
        if (isNotEmpty(propDefs)) {
            for (FlowPropertyDefinition def : propDefs.values()) {
                MissingRequiredTracking.appendRequiredTrackingIfTrue(result,
                    (flowActivityPhase != null && def.getPropertyRequired() == flowActivityPhase)
                        && !flowPropertyProvider.isPropertySet(def.getName())
                        && !def.isAutoCreate(),
                        def.getUiComponentParameterName());
            }
        }
        return result;
    }

}
