package org.amplafi.flow;


/**
 * Represents the way of flow rendering/behavior. This is valuable information for deciding how to render the ui.
 * This is not intended to provide environment information ( like indicate if the user is accessing service using a mobile phone )
 *
 * @author Konstantin Burov (aectann@gmail.com)
 *
 */
public enum FlowAppearance{

    /**
     * Flow behaves as usual, we show all controls and UI elements.
     * Page redirects are not suppressed and current iframe is not closed
     * when there are no more flows to run.
     *
     */
    normal(false, false),

    /**
     * We hide all unneeded UI elements (using CSS for now).
     * Page redirects are suppressed and current iframe is closed when
     * there are no more flows to run.
     *
     * Really should be named 'nested', 'embedded', or 'popup'
     *
     * NOTE: name is referenced in InsertionPoint.js
     */
    minimized(true, true),
    /**
     * This is an apiCall. No UI is expected to be generated.
     */
    apiCall(true, true);

    /**
     *  If true then rendering engine should make current iframe/window to close.
     */
    private boolean closeFrameOnFinish;

    /**
     *  If true then no redirects to default after page or any other page should happen.
     */
    private boolean suppressPageRedirects;

    private FlowAppearance(boolean closeFrameOnFinish, boolean suppressPageRedirects){
        this.closeFrameOnFinish = closeFrameOnFinish;
        this.suppressPageRedirects = suppressPageRedirects;
    }

    public boolean isCloseFrameOnFinish() {
        return closeFrameOnFinish;
    }

    public boolean isSuppressPageRedirects() {
        return suppressPageRedirects;
    }
}
