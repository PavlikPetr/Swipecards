package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.topface.Static;
import com.topface.topface.utils.BannersConfig;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.LocaleConfig;

import java.util.UUID;

/**
 * Класс для хранения в SharedPreferences тех настроек, которые обязательны для работы приложения,
 * но которые можно изменить в рантайме. Нужно прежде всего для редакторских функций
 */
public class AppConfig extends AbstractConfig {

    /**
     * Версия конфига. Если поменять эту цифру, то уже сохраненные настройки сбросятся и будут установлены новые значения по умолчанию
     * Это обязательно делать, если вы например поменяли URL API или версию API
     */
    public static final int APP_CONFIG_VERSION = 1;

    public static final String BASE_CONFIG_SETTINGS = "base_config_settings";
    public static final String DATA_API_URL = "data_api_url";
    private static final String DATA_API_REVISION = "data_api_revision";
    public static final String DATA_AUTH_VK_API = "data_auth_vk_api";
    public static final String DATA_AUTH_FB_API = "data_auth_fb_api";
    private static final String DATA_EDITOR_MODE = "data_editor_mode";
    private static final String DATA_DEBUG_MODE = "data_debug_mode";
    private static final String DATA_APP_CONFIG_VERSION = "data_app_config_version";
    private static final String DATA_API_VERSION = "data_api_version";
    public static final String FLOOD_ENDS_TIME = "flood_ens_time";
    private static final String APP_UNIQUE_ID = "app_unique_id";
    private static final String DATA_TEST_NETWORK = "data_test_network_mode";

    private BannersConfig mBannerConfig;
    private LocaleConfig mLocaleConfig;

    public BannersConfig getBannerConfig() {
        return mBannerConfig;
    }

    public LocaleConfig getLocaleConfig() {
        return mLocaleConfig;
    }

    public String getAuthVkApi() {
        return getSettingsMap().getStringField(DATA_AUTH_VK_API);
    }

    public String getAuthFbApi() {
        return getSettingsMap().getStringField(DATA_AUTH_FB_API);
    }

    public void setDebugMode(int position) {
        getSettingsMap().setField(DATA_DEBUG_MODE, position);
    }

    public int getDebugMode() {
        return getSettingsMap().getIntegerField(DATA_DEBUG_MODE);
    }

    public void setEditorMode(int position) {
        getSettingsMap().setField(DATA_EDITOR_MODE, position);
    }

    public int getEditorMode() {
        return getSettingsMap().getIntegerField(DATA_EDITOR_MODE);
    }

    public Integer getApiVersion() {
        return getSettingsMap().getIntegerField(DATA_API_VERSION);
    }

    public String getApiDomain() {
        return getSettingsMap().getStringField(DATA_API_URL);
    }

    public String getApiRevision() {
        return getSettingsMap().getStringField(DATA_API_REVISION);
    }

    public String getAppUniqueId() {
        SettingsMap settingsMap = getSettingsMap();
        String uniqueId = settingsMap.getStringField(APP_UNIQUE_ID);
        if (TextUtils.isEmpty(uniqueId)) {
            uniqueId = UUID.randomUUID().toString();
            settingsMap.setField(APP_UNIQUE_ID, uniqueId);
        }
        return uniqueId;
    }

    public void setTestNetwork(boolean value) {
        getSettingsMap().setField(DATA_TEST_NETWORK, value);
    }

    public boolean getTestNetwork() {
        return getSettingsMap().getBooleanField(DATA_TEST_NETWORK);
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        settingsMap.addStringField(DATA_API_URL, Static.API_URL);
        settingsMap.addIntegerField(DATA_API_VERSION, Static.API_VERSION);
        settingsMap.addStringField(DATA_API_REVISION, null);
        settingsMap.addStringField(DATA_AUTH_VK_API, Static.AUTH_VK_ID);
        settingsMap.addStringField(DATA_AUTH_FB_API, Static.AUTH_FACEBOOK_ID);
        settingsMap.addIntegerField(DATA_EDITOR_MODE, Editor.MODE_USER_FIELD);
        settingsMap.addIntegerField(DATA_DEBUG_MODE, Debug.MODE_EDITOR);
        settingsMap.addLongField(FLOOD_ENDS_TIME, 0l);
        settingsMap.addStringField(APP_UNIQUE_ID, null);
        settingsMap.addBooleanField(DATA_TEST_NETWORK, false);
    }

    public AppConfig(Context context) {
        super(context);
        mBannerConfig = new BannersConfig(context);
        mLocaleConfig = new LocaleConfig(context);
    }

    protected SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(
                BASE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Сохранет текущее состояние конфига, просто записывая данные из памяти
     */
    public void saveConfigAdditional(SharedPreferences.Editor editor) {
        editor.putInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void saveConfigField(String key) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION);
        SettingsField field = getSettingsMap().get(key);
        switch (field.getType()) {
            case String:
                editor.putString(field.key, (String) field.value);
                break;
            case Integer:
                editor.putInt(field.key, (Integer) field.value);
                break;
            case Boolean:
                editor.putBoolean(field.key, (Boolean) field.value);
                break;
            case Long:
                editor.putLong(field.key, (Long) field.value);
                break;
        }
        editor.commit();
        Debug.log("Save AppConfig:" + key + ": " + toString());
    }

    /**
     * Удаляет все настройки из SharedPreferences, дабы применились настройки по умолчанию
     */
    public void resetToDefault() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION);
        for (SettingsField field : getSettingsMap().values()) {
            editor.remove(field.key);
        }
        editor.commit();
        //Возвращаем значения по умолчанию
        resetSettingsMap();
        Debug.log("Reset AppConfig: " + toString());
    }

    /**
     * Сохраняет все настройки, связаные с доступок к API
     *
     * @param url      путь к API
     * @param version  Версия API
     * @param revision Ревизия (будет работать только для тестовых платформ)
     */
    public void setApiUrl(String url, Integer version, String revision) {
        SettingsMap settingsMap = getSettingsMap();
        settingsMap.setField(DATA_API_URL, url);
        settingsMap.setField(DATA_API_VERSION, version);
        settingsMap.setField(DATA_API_REVISION, revision);
    }

    public String getApiUrl() {
        SettingsMap settingsMap = getSettingsMap();
        return settingsMap.getStringField(DATA_API_URL) + "?v=" + settingsMap.getIntegerField(DATA_API_VERSION);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append("\n").append("Version: ").append(APP_CONFIG_VERSION).append("\n");
        for (SettingsField field : getSettingsMap().values()) {
            result.append(field.key).append(": ").append(field.value).append("\n");
        }
        return result.toString();
    }
}
