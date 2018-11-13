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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.amplafi.flow.flowproperty.DataClassDefinition;
import org.amplafi.flow.flowproperty.ExternalPropertyAccessRestriction;
import org.amplafi.flow.flowproperty.FlowPropertyProvider;
import org.amplafi.flow.flowproperty.FlowPropertyValueChangeListener;
import org.amplafi.flow.flowproperty.FlowPropertyValuePersister;
import org.amplafi.flow.flowproperty.PropertyScope;
import org.amplafi.flow.flowproperty.PropertyUsage;
import com.sworddance.util.map.MapKeyed;

/**
 * FlowPropertyExpectation are immutable.
 *
 * Describes:
 *  1) a set of conditions to be applied to 1 ( name specified ) or more ( no name specified )
 *  2)  a property that is expected AND/OR the characteristics of the expected property to the extent desired.
 * This additional pattern matching avoids false positives.
 *
 * An FlowPropertyExpectation looks similar to a {@link FlowPropertyDefinition} but does not have the "completely-defined" constraints that
 * a {@link FlowPropertyDefinition} has.
 *
 * TODO:Also used to describe properties that another FlowProperty needs.
 * @author patmoore
 *
 */
public interface FlowPropertyExpectation extends MapKeyed<String> {
    /**
     * Note: name is also the value to be returned by {@link #getMapKey()}
     * This method is provided for semantic usefulness.
     * @return the name of the property that is expected.
     */
    String getName();
    /**
     * The class of the object returned by a call to a {@link org.amplafi.flow.flowproperty.FlowPropertyProviderWithValues#getProperty(String, Class)}
     * For a complex collection definition this is the top-level collection.
     *
     * This is optional and is only enforced if a non-null is returned.
     * @return the class expected if defined.
     */
    Class<?> getDataClass();
    /**
     * For more complex type definitions, such as collections and maps, this
     * @return
     */
    <DC extends DataClassDefinition> DC getDataClassDefinition();

    /**
     * @return how the property is being used.
     */
    PropertyUsage getPropertyUsage();

    PropertyScope getPropertyScope();

    ExternalPropertyAccessRestriction getExternalPropertyAccessRestriction();
    <FA extends FlowPropertyProvider> FlowPropertyValueProvider<FA> getFlowPropertyValueProvider();
    /**
     *
     * @return the {@link FlowActivityPhase} when the property be required to be available.
     */
    FlowActivityPhase getPropertyRequired();

    Boolean getSaveBack();
    Set<String> getAlternates();
    Boolean getAutoCreate();

    /**
     * @return listeners to be notified when the property changes value.
     */
    List<FlowPropertyValueChangeListener> getFlowPropertyValueChangeListeners();

    /**
     * if {@link #getName()} != null or "" and isNamed() returns true for all possible names this {@link FlowPropertyExpectation} has
     * @param flowPropertyExpectation
     * @return true if the flowPropertyExpectation can be merged with this.
     */
    boolean isApplicable(FlowPropertyExpectation flowPropertyExpectation);
    boolean isNamed(String possibleName);
    // Kostya: switch the definitions here:
    <FA extends FlowPropertyProvider> FlowPropertyValuePersister<FA> getFlowPropertyValuePersister();
    Set<FlowPropertyExpectation> getPropertiesDependentOn();
    FlowPropertyExpectation merge(FlowPropertyExpectation flowPropertyExpectation);
    boolean isAssignableFrom(Class<?> suggestedClass);
    Optional<Object> getInitial();
}
