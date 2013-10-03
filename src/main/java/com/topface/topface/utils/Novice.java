package com.topface.topface.utils;


import android.content.SharedPreferences;

import com.topface.topface.Static;

public class Novice {
    public static final int MIN_LIKES_QUANTITY = 3;

    public static boolean giveNoviceLikes = false;
    public static int giveNoviceLikesQuantity = 0;

    private Boolean showEnergyToSympathies = null;
    private Boolean showSympathy = null;
    private Boolean showSympathiesBonus = null;
    private Boolean showBuySympathies = null;
    private Boolean showFillProfile = null;

    private SharedPreferences mPreferences;

    private static Novice mInstance;
    private boolean flagsInited = false;

    public static Novice getInstance(SharedPreferences preferences) {
        if (mInstance == null) {
            mInstance = new Novice(preferences);
        }
        return mInstance;
    }

    private Novice(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    public void initNoviceFlags() {
        isShowEnergyToSympathies();
        isShowSympathy();
        isShowBuySympathies();
        isShowFillProfile();
        flagsInited = true;
    }

    public boolean isFlagsInitializationProccesed() {
        return flagsInited;
    }


    public boolean isShowEnergyToSympathies() {
        if (showEnergyToSympathies == null) {
            showEnergyToSympathies = getShowEnergyToSympathies();
        }
        return showEnergyToSympathies;
    }

    public boolean isShowSympathy() {
        if (showSympathy == null) {
            showSympathy = mPreferences.getBoolean(Static.PREFERENCES_NOVICE_DATING_SYMPATHY, true);
        }
        return showSympathy;
    }

    public boolean isShowSympathiesBonus() {
        return Novice.giveNoviceLikes;
    }

    public boolean isShowBuySympathies() {
        if (showBuySympathies == null) {
            showBuySympathies = getShowBuySympathies();
        }
        return showBuySympathies;
    }

    public boolean isShowFillProfile() {
        if (showFillProfile == null) {
            showFillProfile = getShowProfile();
        }
        return showFillProfile;
    }

    public boolean isMenuCompleted() {
        return !isShowFillProfile();
    }

    public boolean isDatingCompleted() {
        return !isShowSympathiesBonus() & !isShowBuySympathies() & !isShowSympathy() & !isShowEnergyToSympathies();
    }

    private boolean getShowEnergyToSympathies() {
        if (mPreferences.contains(Static.PREFERENCES_NOVICE_DATING_ENERGY_TO_SYMPATHIES)) {
            return mPreferences.getBoolean(Static.PREFERENCES_NOVICE_DATING_ENERGY_TO_SYMPATHIES, false);
        } else {
            boolean result = mPreferences.contains(Static.PREFERENCES_NOVICE_DATING_ENERGY);
            commitFlagAsync(Static.PREFERENCES_NOVICE_DATING_ENERGY_TO_SYMPATHIES, false);
            return result;
        }
    }

    private boolean getShowBuySympathies() {
        boolean result = mPreferences.getBoolean(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY, true);
        if (result) return true;

        long todayTime = Utils.unixtimeInSeconds();
        long lastTime = mPreferences.getLong(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY_DATE, 0);

        if (lastTime > 0) {
            return (todayTime - lastTime) >= Utils.WEEK_IN_SECONDS;
        } else {
            commitFlagAsync(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY_DATE, todayTime);
            return false;
        }
    }

    private boolean getShowProfile() {
        boolean result = mPreferences.getBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, true);
        if (!result) return false;
        long todayTime = Utils.unixtimeInSeconds();
        long lastTime = mPreferences.getLong(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE_DATE, 0);

        if (lastTime > 0) {
            return (todayTime - lastTime) >= Utils.DAY_IN_SECONDS;
        } else {
            commitFlagAsync(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE_DATE, todayTime);
            return false;
        }
    }

    public void completeShowSympathy() {
        showSympathy = false;
        commitFlagAsync(Static.PREFERENCES_NOVICE_DATING_SYMPATHY, false);
    }

    public void completeShowBuySympathies() {
        showBuySympathies = false;
        commitFlagAsync(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY, false, Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY_DATE, Utils.unixtimeInSeconds());
    }

    public void completeShowFillProfile() {
        showFillProfile = false;
        commitFlagAsync(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, false);
    }

    public void completeShowBatteryBonus() {
        showSympathiesBonus = false;
        Novice.giveNoviceLikes = false;
    }

    public void completeShowEnergyToSympathies() {
        showEnergyToSympathies = false;
        commitFlagAsync(Static.PREFERENCES_NOVICE_DATING_ENERGY_TO_SYMPATHIES, false);
    }

    private void commitFlagAsync(final String key, final boolean value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(key, value);
                editor.commit();
            }
        }).start();
    }

    private void commitFlagAsync(final String key, final long value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong(key, value);
                editor.commit();
            }
        }).start();
    }

    private void commitFlagAsync(final String key, final boolean value, final String key2, final long value2) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(key, value);
                editor.putLong(key2, value2);
                editor.commit();
            }
        }).start();
    }
}
