package com.topface.topface.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.SharedPreferences;
import android.text.format.Time;

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
        showEnergy = getShowEnergy(); //preferences.getBoolean(Static.PREFERENCES_NOVICE_DATING_ENERGY, true);
        showBatteryBonus = !Novice.giveNovicePower;
        showFillProfile = getShowProfile();//preferences.getBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, true);
        
        mPreferences = preferences;
    }

    public boolean isMenuCompleted() {
    	return !showFillProfile;
    }
    
    public boolean isDatingCompleted() {
        return !showBatteryBonus & !showEnergy & !showSympathy;
    }    

    private boolean getShowEnergy() {    	
    	long todayTime = Utils.unixtime();
    	long lastTime = mPreferences.getLong(Static.PREFERENCES_NOVICE_DATING_ENERGY_DATE,0);    	    	

    	if(lastTime > 0) {    		
    		if ((lastTime - todayTime) > Utils.WEEK) {
	    		SharedPreferences.Editor editor = mPreferences.edit();
		    	editor.putLong(Static.PREFERENCES_NOVICE_DATING_ENERGY_DATE,todayTime);
		    	editor.commit();
		    	return true;
    		} else {
    			return false;
    		}
    	} else {    	
	    	SharedPreferences.Editor editor = mPreferences.edit();
	    	editor.putLong(Static.PREFERENCES_NOVICE_DATING_ENERGY_DATE,todayTime);
	    	editor.commit();
	    	return false;
    	}
    }
    
    private boolean getShowProfile() {	
    	boolean result = mPreferences.getBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, true);
    	if (!result) return false;
    	long todayTime = Utils.unixtime();
    	long lastTime = mPreferences.getLong(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE_DATE,0);    	
    	
    	if(lastTime > 0) {    		
    		if ((lastTime - todayTime) > Utils.DAY) {
		    	return true;
    		} else {
    			return false;
    		}
    	} else {    		
		    SharedPreferences.Editor editor = mPreferences.edit();
		    editor.putLong(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE_DATE,todayTime);
		    editor.commit();
	    	return false;
    	}
    }
    
    public void completeShowSympathy() {
    	showSympathy = false;
    	SharedPreferences.Editor editor = mPreferences.edit();
    	editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_SYMPATHY, false);
//    	editor.commit();
    }
    
    public void completeShowEnergy() {
    	showEnergy = false;
//    	SharedPreferences.Editor editor = mPreferences.edit();
//    	editor.putBoolean(Static.PREFERENCES_NOVICE_DATING_ENERGY, false);
//    	editor.commit();
    }
    
    public void completeShowFillProfile() {
    	showFillProfile = false;
    	SharedPreferences.Editor editor = mPreferences.edit();
    	editor.putBoolean(Static.PREFERENCES_NOVICE_MENU_FILL_PROFILE, false);
//    	editor.commit();
    }
    
    public void completeShowBatteryBonus() {
    	showBatteryBonus = false;
//    	Novice.giveNovicePower = false;    	
    }
}
