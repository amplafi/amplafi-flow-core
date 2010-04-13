package org.amplafi.flow.translator;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.amplafi.flow.flowproperty.DataClassDefinitionImpl;
import org.amplafi.json.JsonSelfRenderer;
import org.amplafi.json.IJsonWriter;
import org.amplafi.json.JSONObject;

/**
 * Test {@link org.amplafi.flow.translator.JsonSelfRendererFlowTranslator}.
 * @author Andreas Andreou
 */
public class TestJsonSelfRendererFlowTranslator extends AbstractTestFlowTranslators {
    @Override
    @Test
    protected FlowTranslator createFlowTranslator() {
        JsonSelfRendererFlowTranslator translator = new JsonSelfRendererFlowTranslator();
        translator.setFlowTranslatorResolver(getFlowTranslatorResolver());
        return translator;
    }

    @Override
    protected DataClassDefinitionImpl createDataClassDefinition() {
        FlowTranslator flowTranslator = createFlowTranslator();
        DataClassDefinitionImpl dataClassDefinition = new DataClassDefinitionImpl(Subject.class);
        dataClassDefinition.setFlowTranslator(flowTranslator);
        return dataClassDefinition;
    }

    @DataProvider(name = "flowTranslatorExpectations")
    @Override
    protected Object[][] getFlowTranslatorExpectations() {

        return new Object[][]{
                data(null, null),
                data(new Subject("me"), "{\"name\":\"me\"}"),
        };
    }

    public static class Subject implements JsonSelfRenderer {

        private String name;

        public Subject() {
        }

        public Subject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public IJsonWriter toJson(IJsonWriter jsonWriter) {
            return jsonWriter.object().keyValue("name", getName()).endObject();
        }

        @Override
        public <T> T fromJson(Object object) {
            JSONObject obj = new JSONObject((String) object);
            setName(obj.getString("name"));
            return (T) this;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            return ((Subject)that).name.equals(name);
        }
    }
}
