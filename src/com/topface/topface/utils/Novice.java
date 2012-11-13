package com.topface.topface.utils;

import android.content.SharedPreferences;

import com.topface.topface.Static;

public class Novice {
	
	public static boolean giveNovicePower = false;
        
    public boolean showSympathy;
    public boolean showBatteryBonus;
    public boolean showEnergy;    
    public boolean showFillProfile;
    
    private SharedPreferences mPreferences;

    public Novice(SharedPreferences preferences) {
        showSympathy = preferences.getBoolean(Static.PREFERENCES_NOVICE_DATING_SYMPATHY, true);
        showEnergy = preferences.getBoolean(Static.PREFERENCES_NOVICE_DATING_ENERGY, true);
        showBatteryBonus = Novice.giveNovicePower;
        showFillProfile = preferences.getBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, true);
        
        mPreferences = preferences;
    }

    public boolean isMenuCompleted() {
    	return !showFillProfile;
    }
    
    public boolean isDatingCompleted() {
        return !showBatteryBonus & !showEnergy & !showSympathy;
    }

    public void completeShowSympathy() {
    	showSympathy = false;
    	SharedPreferences.Editor editor = mPreferences.edit();
    	editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_SYMPATHY, false);
    	editor.commit();
    }
    
    public void completeShowEnergy() {
    	showEnergy = false;
    	SharedPreferences.Editor editor = mPreferences.edit();
    	editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_ENERGY, false);
    	editor.commit();
    }
    
    public void completeShowFillProfile() {
    	showFillProfile = false;
    	SharedPreferences.Editor editor = mPreferences.edit();
    	editor.putBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, false);
    	editor.commit();
    }
    
    public void completeShowBatteryBonus() {
    	showBatteryBonus = false;
    	Novice.giveNovicePower = false;    	
    }
}
