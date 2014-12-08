package com.topface.topface.data.experiments;

import com.topface.topface.App;
import com.topface.topface.utils.config.UserConfig;

import org.json.JSONObject;

/**
 * Experiment for showing topface offers instead of purchases and on leaving purchases screen.
 */
public class TopfaceOfferwallRedirect extends BaseExperiment {

    public static final String KEY_EXP_ON_OPEN = "expOnOpen";
    public static final String KEY_EXP_ON_CLOSE = "expOnClose";
    public static final int SHOW_FREQUENCY = 2;

    private boolean mExpOnOpen;
    private boolean mExpOnClose;
    private UserConfig mUserConfig = App.getUserConfig();

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

    public boolean showOrNot() {
        int showCounter = mUserConfig.getTopfaceOfferwallRedirectCounter();
        boolean showOrNot = showCounter < SHOW_FREQUENCY;
        if (showOrNot) {
            mUserConfig.setTopfaceOfferwallRedirectCounter(showCounter + 1);
        } else {
            mUserConfig.setTopfaceOfferwallRedirectCounter(0);
        }
        mUserConfig.saveConfig();
        return !showOrNot;
    }
}
