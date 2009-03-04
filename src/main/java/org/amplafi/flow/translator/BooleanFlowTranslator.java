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

import org.amplafi.flow.FlowActivity;
import org.amplafi.json.renderers.BooleanJsonRenderer;


/**
 *
 *
 */
public class BooleanFlowTranslator extends AbstractFlowTranslator<Boolean> {

    public BooleanFlowTranslator() {
        super(BooleanJsonRenderer.INSTANCE);
        addDeserializedFormClasses(boolean.class);
    }

    /**
     * @see org.amplafi.flow.FlowTranslator#getTranslatedClass()
     */
    @Override
    public Class<Boolean> getTranslatedClass() {
        return Boolean.class;
    }

    @Override
    public Boolean getDefaultObject(FlowActivity flowActivity) {
        return Boolean.FALSE;
    }
}
