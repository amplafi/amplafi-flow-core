/**
 * Copyright 2006-2011 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.launcher;

import java.net.URI;

/**
 * Creates {@link URI} from a {@link FlowLauncher}.
 * @author patmoore
 *
 */
public interface FlowLauncherLinkGenerator {
    /**
     *
     * @param base if null then a relative {@link URI} will be returned.
     * @param flowLauncher
     * @return resulting URI
     */
    URI createURI(URI base, FlowLauncher flowLauncher);
}
