package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.framework.utils.Debug;
import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.Static;
import com.topface.topface.utils.Editor;

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
    public static final String DATA_AUTH_VK_API = "data_auth_vk_api";
    public static final String DATA_AUTH_FB_API = "data_auth_fb_api";
    public static final String FLOOD_ENDS_TIME = "flood_ens_time";
    private static final String DATA_API_REVISION = "data_api_revision";
    private static final String DATA_EDITOR_MODE = "data_editor_mode";
    private static final String DATA_DEBUG_MODE = "data_debug_mode";
    private static final String DATA_APP_CONFIG_VERSION = "data_app_config_version";
    private static final String DATA_API_VERSION = "data_api_version";
    private static final String DATA_TEST_NETWORK = "data_test_network_mode";
    private static final String DATA_APP_OPTIONS = "data_app_options";


    public AppConfig(Context context) {
        super(context);
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        // api url: https://api.topface.com/
        addField(settingsMap, DATA_API_URL, Static.API_URL);
        // api version number
        addField(settingsMap, DATA_API_VERSION, Static.API_VERSION);
        // api revision for test platforms
        addField(settingsMap, DATA_API_REVISION, null);
        // vk api id
        addField(settingsMap, DATA_AUTH_VK_API, Static.AUTH_VK_ID);
        // fb api id
        addField(settingsMap, DATA_AUTH_FB_API, Static.AUTH_FACEBOOK_ID);
        // editor mode from Editor class
        addField(settingsMap, DATA_EDITOR_MODE, Editor.MODE_USER_FIELD);
        // editor mode from Debug class
        addField(settingsMap, DATA_DEBUG_MODE, Debug.MODE_EDITOR);
        // date when flood ends
        addField(settingsMap, FLOOD_ENDS_TIME, 0l);
        // flag for test mode for network errors
        addField(settingsMap, DATA_TEST_NETWORK, false);
        // app options
        addField(settingsMap, DATA_APP_OPTIONS, Static.EMPTY);

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
     *
     * @return api key
     */
    public String getAuthVkApi() {
        return getStringField(getSettingsMap(), DATA_AUTH_VK_API);
    }

    /**
     * Fb Api key
     *
     * @return api key
     */
    public String getAuthFbApi() {
        return getStringField(getSettingsMap(), DATA_AUTH_FB_API);
    }

    /**
     * Debug mode from Debug class
     *
     * @return mode: {@link com.topface.framework.utils.Debug#MODE_DEBUG},
     * {@link com.topface.framework.utils.Debug#MODE_EDITOR},
     * {@link com.topface.framework.utils.Debug#MODE_ALWAYS},
     * {@link com.topface.framework.utils.Debug#MODE_DISABLE}
     */
    public int getDebugMode() {
        return getIntegerField(getSettingsMap(), DATA_DEBUG_MODE);
    }

    /**
     * Debug mode from Debug class
     *
     * @param mode {@link com.topface.framework.utils.Debug#MODE_DEBUG},
     *             {@link com.topface.framework.utils.Debug#MODE_EDITOR},
     *             {@link com.topface.framework.utils.Debug#MODE_ALWAYS},
     *             {@link com.topface.framework.utils.Debug#MODE_DISABLE}
     */
    public void setDebugMode(int mode) {
        setField(getSettingsMap(), DATA_DEBUG_MODE, mode);
    }

    /**
     * Editor mode from Editor class
     *
     * @return mode: {@link com.topface.topface.utils.Editor#MODE_USER_FIELD},
     * {@link com.topface.topface.utils.Editor#MODE_EDITOR},
     * {@link com.topface.topface.utils.Editor#MODE_NOT_EDITOR}
     */
    public int getEditorMode() {
        return getIntegerField(getSettingsMap(), DATA_EDITOR_MODE);
    }

    /**
     * Editor mode from Editor class
     *
     * @param mode {@link com.topface.topface.utils.Editor#MODE_USER_FIELD},
     *             {@link com.topface.topface.utils.Editor#MODE_EDITOR},
     *             {@link com.topface.topface.utils.Editor#MODE_NOT_EDITOR}
     */
    public void setEditorMode(int mode) {
        setField(getSettingsMap(), DATA_EDITOR_MODE, mode);
    }

    /**
     * Api version for requests
     *
     * @return version number
     */
    public Integer getApiVersion() {
        return getIntegerField(getSettingsMap(), DATA_API_VERSION);
    }

    /**
     * Api url for requests in ConnectionManager
     *
     * @return url for request
     */
    public String getApiDomain() {
        return getStringField(getSettingsMap(), DATA_API_URL);
    }

    /**
     * Api revision for test platforms to identify different server code states
     *
     * @return revision id
     */
    public String getApiRevision() {
        return getStringField(getSettingsMap(), DATA_API_REVISION);
    }

    /**
     * Network errors mode
     *
     * @return true if network errors mode switched on
     */
    public boolean getTestNetwork() {
        return getBooleanField(getSettingsMap(), DATA_TEST_NETWORK);
    }

    /**
     * Network errors mode
     *
     * @param value true if need opportunity to switch network errors on and off
     */
    public void setTestNetwork(boolean value) {
        setField(getSettingsMap(), DATA_TEST_NETWORK, value);
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
        setField(settingsMap, DATA_API_URL, url);
        setField(settingsMap, DATA_API_VERSION, version);
        setField(settingsMap, DATA_API_REVISION, revision);
    }

    /**
     * Url for api request with current saved version
     *
     * @return url for requests
     */
    public String getApiUrl() {
        SettingsMap settingsMap = getSettingsMap();
        return getStringField(settingsMap, DATA_API_URL) + "?v=" + getIntegerField(settingsMap, DATA_API_VERSION);
    }

    /**
     * App options
     *
     * @return json data
     */
    public String getAppOptions() {
        return getStringField(getSettingsMap(), DATA_APP_OPTIONS);
    }

    /**
     * Sets app options
     *
     * @param value json
     */
    public void setAppOptions(String value) {
        setField(getSettingsMap(), DATA_APP_OPTIONS, value);
    }



    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append("\n").append("Version: ").append(APP_CONFIG_VERSION).append("\n");
        for (Object field : getSettingsMap().values()) {
            result.append(field.toString());
        }
        return result.toString();
    }

    public void resetAppOptionsData() {
        resetAndSaveConfig(DATA_APP_OPTIONS);
    }
}
