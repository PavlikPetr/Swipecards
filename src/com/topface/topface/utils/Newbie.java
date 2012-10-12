package com.topface.topface.utils;

import android.content.SharedPreferences;
import com.topface.topface.Static;

public class Newbie {
    // Data
    public boolean profile;
    public boolean dating;
    public boolean likes;
    public boolean tops;
    public boolean free_energy;
    public boolean buy_energy;
    public boolean rate_it;

    public Newbie(SharedPreferences preferences) {
        // dashboard
        profile = preferences.getBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_PROFILE, false);
        dating = preferences.getBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_DATING, false);
        likes = preferences.getBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_LIKES, false);
        tops = preferences.getBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_TOPS, false);

        // dating
        free_energy = preferences.getBoolean(Static.PREFERENCES_NEWBIE_DATING_FREE_ENERGY, false);
        buy_energy = preferences.getBoolean(Static.PREFERENCES_NEWBIE_DATING_BUY_ENERGY, false);
        rate_it = preferences.getBoolean(Static.PREFERENCES_NEWBIE_DATING_RATE_IT, false);
/*
    profile = false;
    dating = false;
    likes = false;
    tops = false;
    
    free_energy = false;
    buy_energy = false;
    rate_it = false;
*/
    }

    public boolean isDashboardCompleted() {
        return profile & dating & likes & tops;
    }

    public boolean isDatingCompleted() {
        return free_energy & buy_energy & rate_it;
    }

}
