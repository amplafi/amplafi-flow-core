package org.amplafi.flow.translator;

import java.util.Calendar;
import java.util.Date;

import org.amplafi.json.renderers.CalendarJsonRenderer;


public class CalendarFlowTranslator extends AbstractFlowTranslator<Calendar> {

    public CalendarFlowTranslator() {
        super(CalendarJsonRenderer.INSTANCE);
        this.addDeserializedFormClasses(Calendar.class, Date.class);
    }

    @Override
    public Class<Calendar> getTranslatedClass() {
        return Calendar.class;
    }
}
