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

/**
 * Interface that defines access to the transaction object currently active.
 *
 * @author Patrick Moore
 *
 */
public interface FlowTx {

    <T> boolean flushIfNeeded(T... entities);

    <T, K> T load(Class<? extends T> clazz, K entityId, boolean nullIdReturnsNull);

    <T, K> T get(Class<? extends T> clazz, K entityId, boolean nullIdReturnsNull);

    void delete(Object entity);

    /**
     * @param entity
     * @return the object in db on success, may not be entity.
     */
    <T> T saveOrUpdate(T entity);
}
