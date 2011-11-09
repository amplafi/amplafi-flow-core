/**
 * Copyright 2006-2011 by Amplafi. All rights reserved.
 * Confidential.
 */
package org.amplafi.flow.launcher;

import java.net.URI;

/**
 * @author patmoore
 *
 */
public interface FlowLauncherLinkGenerator {
    URI createURI(URI base, FlowLauncher flowLauncher);
}
