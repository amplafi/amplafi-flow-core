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
package org.amplafi.flow.validation;

import org.amplafi.flow.FlowActivityPhase;
import org.amplafi.flow.FlowStepDirection;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.validation.FlowValidationResult;

/**
 * Allows for complex validation.
 *
 * TODO:
 * 1) mechanism to indicate which properties the validator depends on (so the previous success/fail flag can be invalidated )
 * 2) mechanism to preserve result of validation result for duration of request.
 *
 * @author patmoore
 * @param <FPP>
 *
 */
public interface FlowValidationResultProvider<FPP extends FlowPropertyProvider> {

    FlowValidationResult getFlowValidationResult(FPP flowPropertyProvider, FlowActivityPhase flowActivityPhase, FlowStepDirection flowStepDirection);
}
