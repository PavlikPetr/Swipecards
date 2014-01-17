package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.topface.Static;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;

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

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        // api url: https://api.topface.com/
        settingsMap.addStringField(DATA_API_URL, Static.API_URL);
        // api version number
        settingsMap.addIntegerField(DATA_API_VERSION, Static.API_VERSION);
        // api revision for test platforms
        settingsMap.addStringField(DATA_API_REVISION, null);
        // vk api id
        settingsMap.addStringField(DATA_AUTH_VK_API, Static.AUTH_VK_ID);
        // fb api id
        settingsMap.addStringField(DATA_AUTH_FB_API, Static.AUTH_FACEBOOK_ID);
        // editor mode from Editor class
        settingsMap.addIntegerField(DATA_EDITOR_MODE, Editor.MODE_USER_FIELD);
        // editor mode from Debug class
        settingsMap.addIntegerField(DATA_DEBUG_MODE, Debug.MODE_EDITOR);
        // date when flood ends
        settingsMap.addLongField(FLOOD_ENDS_TIME, 0l);
        // unique generated id for current app install
        settingsMap.addStringField(APP_UNIQUE_ID, null);
        // flag for test mode for network errors
        settingsMap.addBooleanField(DATA_TEST_NETWORK, false);
    }

    public AppConfig(Context context) {
        super(context);
    }

    protected SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(
                BASE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Saves current version of config to trace relevance
     */
    public void saveConfigAdditional(SharedPreferences.Editor editor) {
        editor.putInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION);
    }

    /**
     * Vk Api key
     * @return api key
     */
    public String getAuthVkApi() {
        return getSettingsMap().getStringField(DATA_AUTH_VK_API);
    }

    /**
     * Fb Api key
     * @return api key
     */
    public String getAuthFbApi() {
        return getSettingsMap().getStringField(DATA_AUTH_FB_API);
    }

    /**
     * Debug mode from Debug class
     * @param mode {@link com.topface.topface.utils.Debug#MODE_DEBUG},
     * {@link com.topface.topface.utils.Debug#MODE_EDITOR},
     * {@link com.topface.topface.utils.Debug#MODE_ALWAYS},
     * {@link com.topface.topface.utils.Debug#MODE_DISABLE}
     */
    public void setDebugMode(int mode) {
        getSettingsMap().setField(DATA_DEBUG_MODE, mode);
    }

    /**
     * Debug mode from Debug class
     * @return mode: {@link com.topface.topface.utils.Debug#MODE_DEBUG},
     * {@link com.topface.topface.utils.Debug#MODE_EDITOR},
     * {@link com.topface.topface.utils.Debug#MODE_ALWAYS},
     * {@link com.topface.topface.utils.Debug#MODE_DISABLE}
     */
    public int getDebugMode() {
        return getSettingsMap().getIntegerField(DATA_DEBUG_MODE);
    }

    /**
     * Editor mode from Editor class
     * @param mode {@link com.topface.topface.utils.Editor#MODE_USER_FIELD},
     * {@link com.topface.topface.utils.Editor#MODE_EDITOR},
     * {@link com.topface.topface.utils.Editor#MODE_NOT_EDITOR}
     */
    public void setEditorMode(int mode) {
        getSettingsMap().setField(DATA_EDITOR_MODE, mode);
    }

    /**
     * Editor mode from Editor class
     * @return mode: {@link com.topface.topface.utils.Editor#MODE_USER_FIELD},
     * {@link com.topface.topface.utils.Editor#MODE_EDITOR},
     * {@link com.topface.topface.utils.Editor#MODE_NOT_EDITOR}
     */
    public int getEditorMode() {
        return getSettingsMap().getIntegerField(DATA_EDITOR_MODE);
    }

    /**
     * Api version for requests
     * @return version number
     */
    public Integer getApiVersion() {
        return getSettingsMap().getIntegerField(DATA_API_VERSION);
    }

    /**
     * Api url for requests in ConnectionManager
     * @return url for request
     */
    public String getApiDomain() {
        return getSettingsMap().getStringField(DATA_API_URL);
    }

    /**
     * Api revision for test platforms to identify different server code states
     * @return revision id
     */
    public String getApiRevision() {
        return getSettingsMap().getStringField(DATA_API_REVISION);
    }

    /**
     * Unique id for app. Generated once for installation and saved for further use
     * @return app id
     */
    public String getAppUniqueId() {
        SettingsMap settingsMap = getSettingsMap();
        String uniqueId = settingsMap.getStringField(APP_UNIQUE_ID);
        if (TextUtils.isEmpty(uniqueId)) {
            uniqueId = UUID.randomUUID().toString();
            settingsMap.setField(APP_UNIQUE_ID, uniqueId);
        }
        return uniqueId;
    }

    /**
     * Network errors mode
     * @param value true if need opportunity to switch network errors on and off
     */
    public void setTestNetwork(boolean value) {
        getSettingsMap().setField(DATA_TEST_NETWORK, value);
    }

    /**
     * Network errors mode
     * @return true if network errors mode switched on
     */
    public boolean getTestNetwork() {
        return getSettingsMap().getBooleanField(DATA_TEST_NETWORK);
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

    /**
     * Url for api request with current saved version
     * @return url for requests
     */
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
