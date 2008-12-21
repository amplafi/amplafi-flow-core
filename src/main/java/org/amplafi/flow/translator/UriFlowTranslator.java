/**
 * Copyright 2006-8 by Amplafi, Inc.
 */
package org.amplafi.flow.translator;

import java.net.URI;

import org.amplafi.json.renderers.UriJsonRenderer;

/**
 *
 *
 */
public class UriFlowTranslator extends AbstractFlowTranslator<URI> {

    public UriFlowTranslator() {
        super(UriJsonRenderer.INSTANCE);
    }
}
