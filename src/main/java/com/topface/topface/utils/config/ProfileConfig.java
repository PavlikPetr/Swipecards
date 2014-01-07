package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.topface.Static;
import com.topface.topface.utils.CacheProfile;

/**
 * Created by kirussell on 06.01.14.
 * Config for data related to User Profile
 * To add new data for store:
 * 1. add new data in newSettingsMap() method using generateKey(String name)
 * 2. create set and get methods for data to add and retrieve data from SettingsMap
 * use generateKey(String name)
 */
public class ProfileConfig extends AbstractConfig {

    private static final String PROFILE_CONFIG_SETTINGS = "profile_config_settings";
    public static final String DATA_PROFILE_PIN_CODE = "data_profile_pin_code";

    public ProfileConfig(Context context) {
        super(context);
    }

    @Override
    protected SettingsMap newSettingsMap() {
        SettingsMap settingsMap = new SettingsMap();

        settingsMap.addStringField(generateKey(DATA_PROFILE_PIN_CODE), Static.EMPTY);

        return settingsMap;
    }

    @Override
    SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(
                PROFILE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Use this method to create key which will be related to current user
     *
     * @param name data field name
     * @return new key which contains user id
     */
    private String generateKey(String name) {
        return CacheProfile.uid + Static.AMPERSAND + name;
    }

    public boolean setPinCode(String pinCode) {
        return getSettingsMap().setField(generateKey(DATA_PROFILE_PIN_CODE), pinCode);
    }

    public String getPinCode() {
        return getSettingsMap().getStringField(generateKey(DATA_PROFILE_PIN_CODE));
    }
}