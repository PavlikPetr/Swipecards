package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.Static;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Created by onikitin on 12.01.15.
 * Класс для работы с UserConfig. Разделяет "старый" конфиг на несколько новых уникальных,
 * перерабатывает текущий конфиг после смены e-mail
 */
public class UserConfigConverter {

    private ArrayList<String> mLoginList = new ArrayList<>();
    private Map<String, ?> mOldConfidFields;
    private int mCurrentConfigNumber = 0;
    private UserConfig mMainUserConfig;
    private String mCurrentLogin;

    public UserConfigConverter() {
    }

    public UserConfigConverter(String mCurrentLogin) {
        mOldConfidFields = getOldConfig().getAll();
        this.mCurrentLogin = mCurrentLogin;
    }

    public void convertConfig() {
        new BackgroundThread() {
            @Override
            public void execute() {
                getAllLogins();
                separateConfig();
                removeOldConfig();
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
     * Создает новый конфиг логина полученного в методе getAllLogins().
     * Созданный конфиг заполняется соответствующими данными из старого общего конфига.После того,
     * как все поля старого конфига будет пройдены метод вызовет сам себя для следующего логина.
     */
    public void separateConfig() {
        UserConfig uniqueConfig = createUnicleNewConfig(mLoginList.get(mCurrentConfigNumber));
        Iterator iterator = ((HashMap) uniqueConfig.getSettingsMap()).entrySet().iterator();
        String oldKey = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            oldKey = generateOldKey(mLoginList.get(mCurrentConfigNumber), (String) entry.getKey());
            if (mOldConfidFields.containsKey(oldKey)) {
                uniqueConfig.addField(uniqueConfig.getSettingsMap(), (String) entry.getKey(), mOldConfidFields.get(oldKey));
                mOldConfidFields.remove(oldKey);
            }
            if (!iterator.hasNext() && mOldConfidFields.size() != 0) {
                mCurrentConfigNumber++;
                separateConfig();
            }
        }
        if (oldKey.contains(mCurrentLogin)) {
            setMainUserConfig(uniqueConfig);
        }
        uniqueConfig.commitConfig();

    }

    /**
     * Извлекает из старого общего конфига все логины, для которых были созданы поля
     */
    public boolean getAllLogins() {
        Set<String> keys = mOldConfidFields.keySet();
        StringBuilder login = new StringBuilder();
        for (String key : keys) {
            login.append(getConfigPartFromKey(key));
            if (mLoginList.size() == 0) {
                mLoginList.add(login.toString());
            } else {
                if (!mLoginList.contains(login.toString())) {
                    mLoginList.add(login.toString());
                }
            }
            login.setLength(0);
        }
        return true;
    }

    private String getConfigPartFromKey(String key) {
        return key.substring(0, key.lastIndexOf("&"));
    }

    private UserConfig createUnicleNewConfig(String login) {
        return new UserConfig(login.substring(login.indexOf("&") + 1), App.getContext());
    }

    private String generateOldKey(String configPart, String key) {
        return configPart + Static.AMPERSAND + key;
    }

    /**
     * Сохраняет конфиг для логина под которым была осуществлена авторизация, как главный.
     * С ним будет осуществляться работа приложения.
     */
    private void setMainUserConfig(UserConfig mMainUserConfig) {
        this.mMainUserConfig = mMainUserConfig;
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
        oldConfigPreferences.edit().clear().apply();
        newConfig.saveConfig();
    }

    private SharedPreferences getConfigPreferencesByLogin(String login) {
        return App.getContext().getSharedPreferences(
                UserConfig.PROFILE_CONFIG_SETTINGS +
                        Static.AMPERSAND + login,
                Context.MODE_PRIVATE);
    }

}
