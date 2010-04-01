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
package org.amplafi.flow.launcher;

/**
 * Used to hook up the UI framework (or some other framework that can provide values )
 * Primarily used during launching
 * @author patmoore
 *
 */
public interface ValueFromBindingProvider {
    /**
     * Used to map object properties into flowState values.
     * <p/>
     * TODO: current implementation ignores lookup and just returns the root object.
     *
     * @param root the root object.
     * @param lookup the property 'path' that will result in the value to be assigned.
     * For example, 'bar.foo' will look for a 'bar' property in the "current" object.
     * That 'bar' object should have a 'foo' property. The 'foo' property will be returned
     * as the value to be stored into the FlowState map.
     * @return the value that root and lookup bind to.
     */
    Object getValueFromBinding(Object root, String lookup);
}
