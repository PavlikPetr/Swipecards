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

    private static Novice mInstance;

    public static Novice getInstance() {
        if (mInstance == null) {
            mInstance = new Novice();
        }
        return mInstance;
    }

    private Novice() {
        mConfig = App.getUserConfig();
    }

    public void initNoviceFlags() {
        isShowSympathy();
        isShowBuySympathies();
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
        boolean result = config.getNoviceBuySympathy();
        if (result) return true;

        long todayTime = Utils.unixtimeInSeconds();
        long lastTime = config.getNoviceBuySympathyDate();

        if (lastTime > 0) {
            return (todayTime - lastTime) >= Utils.WEEK_IN_SECONDS;
        } else {
            config.setNoviceBuySympathyDate(todayTime);
            config.saveConfig();
            return false;
        }
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
