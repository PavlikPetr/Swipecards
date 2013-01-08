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

    public static Novice getInstance(SharedPreferences preferences) {
        if (mInstance == null) {
            mInstance = new Novice(preferences);
        }
        return mInstance;
    }

    private Novice(SharedPreferences preferences) {
        mPreferences = preferences;
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
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_ENERGY_TO_SYMPATHIES, false);
            editor.commit();
            return result;
        }
    }

    private boolean getShowBuySympathies() {
        boolean result = mPreferences.getBoolean(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY, true);
        if (result) return true;

        long todayTime = Utils.unixtime();
        long lastTime = mPreferences.getLong(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY_DATE, 0);

        if (lastTime > 0) {
            return (todayTime - lastTime) >= Utils.WEEK;
        } else {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putLong(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY_DATE, todayTime);
            editor.commit();
            return false;
        }
    }

    private boolean getShowProfile() {
        boolean result = mPreferences.getBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, true);
        if (!result) return false;
        long todayTime = Utils.unixtime();
        long lastTime = mPreferences.getLong(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE_DATE, 0);

        if (lastTime > 0) {
            return (todayTime - lastTime) >= Utils.DAY;
        } else {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putLong(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE_DATE, todayTime);
            editor.commit();
            return false;
        }
    }

    public void completeShowSympathy() {
        showSympathy = false;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_SYMPATHY, false);
        editor.commit();
    }

    public void completeShowBuySympathies() {
        showBuySympathies = false;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY_DATE, Utils.unixtime());
        editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_BUY_SYMPATHY, false);
        editor.commit();
    }

    public void completeShowFillProfile() {
        showFillProfile = false;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, false);
        editor.commit();
    }

    public void completeShowBatteryBonus() {
        showSympathiesBonus = false;
        Novice.giveNoviceLikes = false;
    }

    public void completeShowEnergyToSympathies() {
        showEnergyToSympathies = false;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_ENERGY_TO_SYMPATHIES, false);
        editor.commit();
    }
}
