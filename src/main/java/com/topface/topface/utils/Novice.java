package com.topface.topface.utils;


import com.topface.topface.App;
import com.topface.topface.utils.config.UserConfig;

public class Novice {
    public static final int MIN_LIKES_QUANTITY = 3;

    public static boolean giveNoviceLikes = false;
    public static int giveNoviceLikesQuantity = 0;

    private final UserConfig mConfig;

    private Boolean mShowSympathy = null;
    private Boolean mShowBuySympathies = null;

    public Novice() {
        mConfig = App.getUserConfig();
    }

    public boolean isShowSympathy() {
        if (mShowSympathy == null) {
            mShowSympathy = mConfig.getNoviceSympathy();
        }
        return mShowSympathy;
    }

    public boolean isShowSympathiesBonus() {
        return Novice.giveNoviceLikes;
    }

    public boolean isShowBuySympathies() {
        if (mShowBuySympathies == null) {
            mShowBuySympathies = getShowBuySympathies();
        }
        return mShowBuySympathies;
    }

    public boolean isDatingCompleted() {
        return !isShowSympathiesBonus() && !isShowBuySympathies() && !isShowSympathy();
    }

    private boolean getShowBuySympathies() {
        UserConfig config = mConfig;
        if (config.getNoviceBuySympathy()) return true;
        long lastTime = config.getNoviceBuySympathyDate();
        return (lastTime > 0) && (Utils.unixtimeInSeconds() - lastTime) >= Utils.WEEK_IN_SECONDS;
    }

    public void completeShowSympathy() {
        mShowSympathy = false;
        mConfig.setNoviceSympathy(false);
        mConfig.saveConfig();
    }

    public void completeShowBuySympathies() {
        mShowBuySympathies = false;
        mConfig.setNoviceBuySympathy(false);
        mConfig.setNoviceBuySympathyDate(Utils.unixtimeInSeconds());
        mConfig.saveConfig();
    }

    public void completeShowNoviceSympathiesBonus() {
        Novice.giveNoviceLikes = false;
    }
}
