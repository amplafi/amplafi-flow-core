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

package org.amplafi.flow.translator;

import org.amplafi.json.renderers.EnumJsonRenderer;
import org.amplafi.flow.FlowTranslator;

/**
 * @author patmoore
 * @param <T>
 *
 */
public class EnumFlowTranslator<T> extends AbstractFlowTranslator<T> implements InstanceSpecificFlowTranslator<T>{

    private Class<T> translatedClass;

    @SuppressWarnings("unchecked")
    public EnumFlowTranslator() {
        super(EnumJsonRenderer.INSTANCE);
        this.translatedClass = (Class<T>) Enum.class;
    }
    @SuppressWarnings("unchecked")
    public EnumFlowTranslator(EnumFlowTranslator<?> original, Class<T> translatedClass) {
        super(original);
        this.jsonRenderer = EnumJsonRenderer.INSTANCE;
        this.translatedClass = translatedClass;
    }

    /**
     * @return the translatedClass
     */
    @Override
    public Class<T> getTranslatedClass() {
        return translatedClass;
    }
    /**
     * @see org.amplafi.flow.translator.InstanceSpecificFlowTranslator#resolveFlowTranslator(java.lang.Class)
     */
    @Override
    public <V> FlowTranslator<V> resolveFlowTranslator(Class<V> clazz) {
        EnumFlowTranslator<V> narrowed = new EnumFlowTranslator<V>(this, clazz);
        return narrowed;
    }
}