package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.topface.Static;
import com.topface.topface.utils.social.AuthToken;

/**
 * Created by kirussell on 06.01.14.
 * Config for data related to User Profile
 * Unique key for data based on AuthToken (social net user id),
 * so you need to call onAuthTokenReceived()
 *
 * To add new data for store:
 * 1. add new data in fillSettingsMap() method using generateKey(String name)
 * 2. create set and get methods for data to add and retrieve data from SettingsMap
 * use generateKey(String name)
 */
public class ProfileConfig extends AbstractConfig {
    private static final String PROFILE_CONFIG_SETTINGS = "profile_config_settings";
    public static final String DATA_PROFILE = "data_profile_user_data";
    public static final String DATA_PIN_CODE = "data_profile_pin_code";
    private static final String DATA_OPTIONS = "data_options";

    public ProfileConfig(Context context) {
        super(context);
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        settingsMap.addStringField(generateKey(DATA_PROFILE), Static.EMPTY);
        settingsMap.addStringField(generateKey(DATA_OPTIONS), Static.EMPTY);
        settingsMap.addStringField(generateKey(DATA_PIN_CODE), Static.EMPTY);
    }

    @Override
    protected boolean canInitData() {
        return !AuthToken.getInstance().isEmpty();
    }

    @Override
    protected SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(
                PROFILE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Use this method to create key which will be related to current user
     * @param name data field name
     * @return new key which contains user id
     */
    private String generateKey(String name) {
        AuthToken token = AuthToken.getInstance();
        return token.getSocialNet() +
                Static.AMPERSAND + token.getUserSocialId() +
                Static.AMPERSAND + name;
    }

    void onAuthTokenReceived() {
        initData();
    }

    // Pincode

    public boolean setPinCode(String pinCode) {
        return getSettingsMap().setField(generateKey(DATA_PIN_CODE), pinCode);
    }

    public String getPinCode() {
        return getSettingsMap().getStringField(generateKey(DATA_PIN_CODE));
    }

    // Profile

    public boolean setProfileData(String profileResponseJson) {
        return getSettingsMap().setField(generateKey(DATA_PROFILE), profileResponseJson);
    }

    public String getProfileData() {
        return getSettingsMap().getStringField(generateKey(DATA_PROFILE));
    }

    public void resetProfileData() {
        resetAndSaveConfig(DATA_PROFILE);
    }

    // Options

    public boolean setOptionsData(String     optionsResponseJson) {
        return getSettingsMap().setField(generateKey(DATA_OPTIONS), optionsResponseJson);
    }

    public String getOptionsData() {
        return getSettingsMap().getStringField(generateKey(DATA_OPTIONS));
    }

    public void resetOptionsData() {
        resetAndSaveConfig(DATA_OPTIONS);
    }
}