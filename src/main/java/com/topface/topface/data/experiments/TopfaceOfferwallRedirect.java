package com.topface.topface.data.experiments;

import android.content.Intent;
import android.os.Parcel;
import android.text.TextUtils;

import com.topface.offerwall.publisher.TFOfferwallActivity;
import com.topface.topface.App;
import com.topface.topface.ui.fragments.BonusFragment;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import org.json.JSONObject;

/**
 * Experiment for showing topface offers instead of purchases and on leaving purchases screen.
 */
public class TopfaceOfferwallRedirect extends BaseExperiment {

    public static final String TOPFACE_OFFERWAL_REDIRECT = "topface_offerwall_redirect";

    public static Creator<TopfaceOfferwallRedirect> CREATOR = new Creator<TopfaceOfferwallRedirect>() {
        @Override
        public TopfaceOfferwallRedirect createFromParcel(Parcel source) {
            return new TopfaceOfferwallRedirect(source);
        }

        @Override
        public TopfaceOfferwallRedirect[] newArray(int size) {
            return new TopfaceOfferwallRedirect[size];
        }
    };

    public static final String KEY_EXP_ON_OPEN = "expOnOpen";
    public static final String KEY_EXP_ON_CLOSE = "expOnClose";

    private boolean mExpOnOpen;
    private boolean mExpOnClose;
    private boolean mIsCompleted;
    private UserConfig mUserConfig = App.getUserConfig();


    public TopfaceOfferwallRedirect() {
    }

    protected TopfaceOfferwallRedirect(Parcel in) {
        super(in);
        mExpOnOpen = in.readByte() == 1;
        mExpOnClose = in.readByte() == 1;
        mIsCompleted = in.readByte() == 1;
    }

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
        boolean showOrNot = showCounter < UserConfig.TOPFACE_OFFERWALL_REDIRECTION_FREQUENCY;
        mUserConfig.incrementTopfaceOfferwallRedirectCounter();
        mUserConfig.saveConfig();
        return !showOrNot;
    }

    public void setComplited(boolean complited) {
        mIsCompleted = complited;
    }

    /**
     * Check completeness when Topface offerwall was opened instead of PurchasesActivity
     *
     * @param intent Intent for PurchasesActivity. Contains all extras that intent for Topface offerwall contained.
     */
    public void setCompletedByIntent(Intent intent) {
        if (intent.hasExtra(TFOfferwallActivity.PAYLOAD)) {
            mIsCompleted = true;
        }
    }

    /**
     * Check completness by broadcast from BonusFragment when topface offerwall is opened.
     *
     * @param intent broadcast from BonusFragment
     */
    public void setCompletedByBroadcast(Intent intent) {
        mIsCompleted = TextUtils.equals(
                intent.getStringExtra(BonusFragment.OFFERWALL_NAME), OfferwallsManager.TFOFFERWALL);
    }

    public boolean isCompleted() {
        return mIsCompleted;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (mExpOnOpen ? 1 : 0));
        dest.writeByte((byte) (mExpOnClose ? 1 : 0));
        dest.writeByte((byte) (mIsCompleted ? 1 : 0));
    }
}
