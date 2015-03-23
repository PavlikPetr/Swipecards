package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.App;
import com.topface.topface.Static;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by onikitin on 12.01.15.
 * Класс для работы с UserConfig. Разделяет "старый" конфиг на несколько новых уникальных,
 * перерабатывает текущий конфиг после смены e-mail
 */
public class UserConfigConverter {

    public enum ConverterState {DEFAULT, PROCESS, DONE}

    private OnUpdateUserConfig mUpdateUserConfig;
    private ArrayList<String> mLoginList = new ArrayList<>();
    private Map<String, ?> mOldConfidFields;
    private UserConfig mMainUserConfig;
    private String mCurrentLogin;
    private ConverterState mConverterState = ConverterState.DEFAULT;

    public UserConfigConverter() {
    }

    public UserConfigConverter(String mCurrentLogin, OnUpdateUserConfig updateUserConfig) {
        mOldConfidFields = getOldConfig().getAll();
        this.mCurrentLogin = mCurrentLogin;
        mUpdateUserConfig = updateUserConfig;
    }

    public void convertConfig() {
        App.getAppConfig().setUserConfigConverted();
        mConverterState = ConverterState.PROCESS;
        new BackgroundThread() {
            @Override
            public void execute() {
                getAllLogins();
                separateConfig();
                App.getAppConfig().saveConfig();
                removeOldConfig();
                if (mUpdateUserConfig != null) {
                    mConverterState = ConverterState.DONE;
                    mUpdateUserConfig.onUpdate();
                }
            }
        };
    }

    private SharedPreferences getOldConfig() {
        return App.getContext().getSharedPreferences(
                UserConfig.PROFILE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE);
    }

    public static boolean hasOldConfig() {
        SharedPreferences preferences = App.getContext().getSharedPreferences(
                UserConfig.PROFILE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE);
        return !(preferences.getAll().size() == 0);
    }

    /**
     * Разбить старый конфиг на несколько новых, уникальных
     */
    public void separateConfig() {
        for (String login : mLoginList) {
            fillUniqueConfig(login);
        }
    }

    /**
     * Создает новый конфиг логина полученного в методе getAllLogins().
     * Созданный конфиг заполняется соответствующими данными из старого общего конфига.
     *
     * @param login платформа и логин пользователя
     */
    private void fillUniqueConfig(String login) {

        UserConfig uniqueConfig;
        String name = getUniqueCofigName(login);
        if (TextUtils.isEmpty(name)) {
            return;
        }

        uniqueConfig = createUniqueNewConfig(name);
        String oldKey = null;
        AbstractConfig.SettingsMap settingsMap = uniqueConfig.getSettingsMap();
        for (String key : settingsMap.keySet()) {
            oldKey = generateOldKey(login, key);
            if (mOldConfidFields.containsKey(oldKey)) {
                uniqueConfig.addField(settingsMap, key, mOldConfidFields.get(oldKey));
                mOldConfidFields.remove(oldKey);
            }
        }

        if (oldKey != null && oldKey.contains(mCurrentLogin)) {
            setMainUserConfig(uniqueConfig);
        }
        uniqueConfig.commitConfig();
    }

    private String getUniqueCofigName(String login) {
        return login.substring(login.indexOf("&") + 1);
    }


    /**
     * Извлекает из старого общего конфига все логины, для которых были созданы поля
     */
    public boolean getAllLogins() {
        for (String key : mOldConfidFields.keySet()) {
            String login = getConfigPartFromKey(key);
            if (login != null && !mLoginList.contains(login)) {
                mLoginList.add(login);
            }
        }
        return true;
    }

    private String getConfigPartFromKey(String key) {
        return key.substring(0, key.lastIndexOf("&"));
    }

    private UserConfig createUniqueNewConfig(String name) {
        return new UserConfig(name, App.getContext());
    }

    private String generateOldKey(String configPart, String key) {
        return configPart + Static.AMPERSAND + key;
    }

    /**
     * Сохраняет конфиг для логина под которым была осуществлена авторизация, как главный.
     * С ним будет осуществляться работа приложения.
     */
    private void setMainUserConfig(UserConfig mainUserConfig) {
        this.mMainUserConfig = mainUserConfig;
    }

    public UserConfig getMainUserConfig() {
        return mMainUserConfig;
    }

    public void removeOldConfig() {
        getOldConfig().edit().clear().apply();
    }

    /**
     * Создает новый конфиг для нового e-mail и копирует из старого конфига.
     * После копирования старый конфиг очищается.
     *
     * @param oldEmail предыдущий e-mail пользователя
     */
    public void rebuildConfig(String oldEmail, String newEmail) {
        SharedPreferences oldConfigPreferences =
                getConfigPreferencesByLogin(oldEmail);
        UserConfig newConfig =
                new UserConfig(newEmail, App.getContext());
        Map<String, ?> configFields = oldConfigPreferences.getAll();
        for (Object o : ((HashMap) newConfig.getSettingsMap()).entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            newConfig.addField(newConfig.getSettingsMap(),
                    (String) entry.getKey(), configFields.get(entry.getKey()));
        }
        newConfig.saveConfig();
        oldConfigPreferences.edit().clear().apply();
    }

    private SharedPreferences getConfigPreferencesByLogin(String login) {
        return App.getContext().getSharedPreferences(
                UserConfig.PROFILE_CONFIG_SETTINGS +
                        Static.AMPERSAND + login,
                Context.MODE_PRIVATE);
    }

    public ConverterState getConverterState() {
        return mConverterState;
    }

    interface OnUpdateUserConfig {

        void onUpdate();

    }

}
