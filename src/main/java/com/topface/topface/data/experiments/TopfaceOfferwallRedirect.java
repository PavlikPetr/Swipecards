package com.topface.topface.data.experiments;

import org.json.JSONObject;

/**
 * Experiment for showing topface offers instead of purchases and on leaving purchases screen.
 */
public class TopfaceOfferwallRedirect extends BaseExperiment {

    public static final String KEY_EXP_ON_OPEN = "expOnOpen";
    public static final String KEY_EXP_ON_CLOSE = "expOnClose";

    private boolean mExpOnOpen;
    private boolean mExpOnClose;

    public boolean isExpOnOpen() {
        return mExpOnOpen;
    }

    public void setExpOnOpen(boolean expOnOpen) {
        this.mExpOnOpen = expOnOpen;
    }

    public boolean isExpOnClose() {
        return mExpOnClose;
    }

    public void setExpOnClose(boolean expOnClose) {
        this.mExpOnClose = expOnClose;
    }

    @Override
    protected String getOptionsKey() {
        return "topfaceOfferwall";
    }

    @Override
    protected void setKeys(JSONObject source) {
        super.setKeys(source);
        setExpOnOpen(source.optBoolean(KEY_EXP_ON_OPEN));
        setExpOnClose(source.optBoolean(KEY_EXP_ON_CLOSE));
    }
}
