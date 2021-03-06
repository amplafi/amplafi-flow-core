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

import org.amplafi.flow.definitions.DefinitionSource;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;

/**
 * A collection of flows that are related in some manner.
 *
 * TODO : Confusion about DefinitionSource v FlowGroup.
 * A Flow can be part of multiple FlowGroups.
 * @author patmoore
 *
 */
public interface FlowGroup extends DefinitionSource, FlowPropertyProvider {

}
