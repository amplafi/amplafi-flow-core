/**
 * Copyright (c) 2008 Amplafi, Inc. Produced as a work for hire by i.Point for
 * Amplafi. All rights reserved. Patents pending.
 */
package org.amplafi.flow;

/**
 * Here should be some useful constants for services.
 *
 * @author sasha nasheeva@gmail.com
 *
 */
public class ServicesConstants {
    // would really like to fold this into FlowConstants.FSNEXT_FLOW
    // as of 3 Jan 2009 doesn't look like the this constant's value is used directly in javascript.
    @Deprecated
    public static final String FLOW_TYPE = "flow";
    @Deprecated // don't really see the advantage / need for this.
    public static final String FLOW_SERVICE_LISTENER = "fs";
    /**
     *
     */
    public static final String COOKIE_OBJECT = "cookieObject";

}
