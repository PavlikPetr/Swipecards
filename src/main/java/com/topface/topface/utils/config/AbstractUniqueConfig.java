package com.topface.topface.utils.config;

import android.content.Context;

/**
 * Created by kirussell on 19.05.2014.
 * Configuration that can be unified by generated unique key
 */
public abstract class AbstractUniqueConfig extends AbstractConfig {

    public AbstractUniqueConfig(Context context) {
        super(context);
    }

    @Override
    protected void addField(SettingsMap settingsMap, String key, Object defaultValue) {
        super.addField(settingsMap, generateUniqueKey(key), defaultValue);
    }

    @Override
    protected boolean setField(SettingsMap settingsMap, String key, Object defaultValue) {
        return super.setField(settingsMap, generateUniqueKey(key), defaultValue);
    }

    @Override
    protected String getStringField(SettingsMap settingsMap, String key) {
        return super.getStringField(settingsMap, generateUniqueKey(key));
    }

    @Override
    protected int getIntegerField(SettingsMap settingsMap, String key) {
        return super.getIntegerField(settingsMap, generateUniqueKey(key));
    }

    @Override
    protected boolean getBooleanField(SettingsMap settingsMap, String key) {
        return super.getBooleanField(settingsMap, generateUniqueKey(key));
    }

    @Override
    protected Long getLongField(SettingsMap settingsMap, String key) {
        return super.getLongField(settingsMap, generateUniqueKey(key));
    }

    @Override
    protected Double getDoubleField(SettingsMap settingsMap, String key) {
        return super.getDoubleField(settingsMap, generateUniqueKey(key));
    }

    @Override
    protected void resetAndSaveConfig(String key) {
        super.resetAndSaveConfig(generateUniqueKey(key));
    }

    /**
     * Use this method to create key which will be related to current user
     *
     * @param name data field name
     * @return new key which contains user id
     */
    protected abstract String generateUniqueKey(String name);
}
