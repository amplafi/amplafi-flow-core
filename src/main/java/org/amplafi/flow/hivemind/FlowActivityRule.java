/*
 * Created on May 28, 2007
 * Copyright 2006-2008 by Amplafi
 */
package org.amplafi.flow.hivemind;

import org.amplafi.flow.impl.FlowActivityImpl;
import org.amplafi.hivemind.util.AttributeBasedCreateObjectRule;


/**
 * Provide rule for creating a FlowActivity subclass instance from
 * a hivemind rule.
 * @author Patrick Moore
 */
public class FlowActivityRule extends AttributeBasedCreateObjectRule {
    public FlowActivityRule() {
        super.setAttribute("class");
        super.setDefaultClass(FlowActivityImpl.class.getName());
    }
}
