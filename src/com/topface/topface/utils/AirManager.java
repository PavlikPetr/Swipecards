package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.AirMessagesPopupFragment;

public class AirManager {
    public static final String AIR_TYPE_LAST_TAG = "air_type_last_tag";
    private final Context mContext;
    private int mType;

    public AirManager(Context context) {
        mContext = context;
    }

    public void startFragment(FragmentManager fm) {
        mType =  getLastFragmentType() == Options.PremiumAirEntity.AIR_MESSAGES ?
                Options.PremiumAirEntity.AIR_GUESTS : Options.PremiumAirEntity.AIR_MESSAGES;
        if (AirMessagesPopupFragment.showIfNeeded(fm, mType)) {
            setLastFragmentType();
        }
    }

    public int getLastFragmentType() {
        SharedPreferences prefs = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return prefs.getInt(AIR_TYPE_LAST_TAG, Options.PremiumAirEntity.AIR_MESSAGES);
    }

    public void setLastFragmentType() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = mContext.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                prefs.edit().putInt(AIR_TYPE_LAST_TAG, mType).commit();
            }
        }).start();
    }
}
