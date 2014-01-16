package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.topface.Static;

/**
 * Created by kirussell on 14.01.14.
 * Config for data in current session (from login[receive AuthToken] to logout[AuthorizationManager])
 */
public class SessionConfig extends AbstractConfig {
    private static final String SESSION_CONFIG_SETTINGS = "session_config_settings";

    private static final String DATA_PROFILE = "data_profile_user_data";
    private static final String DATA_OPTIONS = "data_options";
    private static final String DATA_GOOGLE_PRODUCTS = "data_google_products";

    public SessionConfig(Context context) {
        super(context);
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        settingsMap.addStringField(DATA_PROFILE, Static.EMPTY);
        settingsMap.addStringField(DATA_OPTIONS, Static.EMPTY);
    }

    @Override
    protected SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(
                SESSION_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    // Profile

    public boolean setProfileData(String profileResponseJson) {
        return getSettingsMap().setField(DATA_PROFILE, profileResponseJson);
    }

    public String getProfileData() {
        return getSettingsMap().getStringField(DATA_PROFILE);
    }

    public void resetProfileData() {
        resetAndSaveConfig(DATA_PROFILE);
    }

    // Options

    public boolean setOptionsData(String optionsResponseJson) {
        return getSettingsMap().setField(DATA_OPTIONS, optionsResponseJson);
    }

    public String getOptionsData() {
        return getSettingsMap().getStringField(DATA_OPTIONS);
    }

    public void resetOptionsData() {
        resetAndSaveConfig(DATA_OPTIONS);
    }

    // Google Products

    public boolean setGoogleProductsData(String googleProductsResponseJson) {
        return getSettingsMap().setField(DATA_GOOGLE_PRODUCTS, googleProductsResponseJson);
    }

    public String getGoogleProductsData() {
        return getSettingsMap().getStringField(DATA_GOOGLE_PRODUCTS);
    }

    public void resetGoogleProductsData() {
        resetAndSaveConfig(DATA_GOOGLE_PRODUCTS);
    }
}
