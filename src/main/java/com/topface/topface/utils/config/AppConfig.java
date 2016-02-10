package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.BuildConfig;
import com.topface.topface.data.social.AppSocialAppsIds;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.transport.scruffy.ScruffyRequestManager;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.ConnectionManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public static final String SCRUFFY_DATA_API_URL = "scruffy_data_api_url";
    public static final String FLOOD_ENDS_TIME = "flood_ens_time";
    private static final String DATA_EDITOR_MODE = "data_editor_mode";
    private static final String DATA_DEBUG_MODE = "data_debug_mode";
    private static final String DATA_APP_CONFIG_VERSION = "data_app_config_version";
    private static final String DATA_TEST_NETWORK = "data_test_network_mode";
    private static final String DATA_APP_OPTIONS = "data_app_options";
    private static final String LAST_FULLSCREEN_TIME = "fullScreeenBanner_last_time";
    private static final String FULLSCREEN_URLS_SET = "fullscreen_urls_string";
    private static final String URL_SEPARATOR = "::";
    public static final String STAGE_LOGIN = "stage_login";
    public static final String STAGE_CHECKED = "stage_checked";
    public static final String AD_ID = "ad_id";
    public static final String DEBUG_CONNECTION = "debug_connection";
    public static final String DEBUG_CONNECTION_CHECKED = "debug_connection_checked";
    private static final String LAST_APP_VERSION = "last_app_version";
    private static final String GCM_REG_ID = "gcm_reg_id";
    public static final String SAVED_EMAIL_LIST = "tf_saved_email_list";
    public static final String SOCIAL_BUTTONS_SETTINGS = "ButtonSettings";
    public static final String CONVERT_CONFIG = "convert_config";
    public static final String POPUP_NOTIFICATION_DISABLE_TIME = "popup_notification_disable_time";
    private static final String DATA_APP_SOCIAL_IDS = "data_app_social_ids";


    public AppConfig(Context context) {
        super(context);
    }

    @Override
    protected void fillSettingsMap(SettingsMap settingsMap) {
        // api url: https://api.topface.com/
        addField(settingsMap, DATA_API_URL, ConnectionManager.API_URL);
        // api url: wss://scruffy.core.tf/
        addField(settingsMap, SCRUFFY_DATA_API_URL, ScruffyRequestManager.API_URL);
        // editor mode from Editor class
        addField(settingsMap, DATA_EDITOR_MODE, Editor.MODE_USER_FIELD);
        // editor mode from Debug class
        addField(settingsMap, DATA_DEBUG_MODE, Debug.MODE_EDITOR);
        // date when flood ends
        addField(settingsMap, FLOOD_ENDS_TIME, 0l);
        // flag for test mode for network errors
        addField(settingsMap, DATA_TEST_NETWORK, false);
        // app options
        addField(settingsMap, DATA_APP_OPTIONS, Utils.EMPTY);
        // last fullscreen time
        addField(settingsMap, LAST_FULLSCREEN_TIME, 0L);
        // fullscreen urls
        addField(settingsMap, FULLSCREEN_URLS_SET, Utils.EMPTY);
        //stage login for admin
        addField(settingsMap, STAGE_LOGIN, Utils.EMPTY);
        //state of checkbox of stagelogin
        addField(settingsMap, STAGE_CHECKED, false);
        //ad id from google play services
        addField(settingsMap, AD_ID, null);
        //debug connection type
        addField(settingsMap, DEBUG_CONNECTION, 0);
        //debug connection is checked
        addField(settingsMap, DEBUG_CONNECTION_CHECKED, false);
        //Last app version
        addField(settingsMap, LAST_APP_VERSION, 0);
        //GCM registration id
        addField(settingsMap, GCM_REG_ID, Utils.EMPTY);
        // список всех email, с котороми удачно прошла авторизация в стандартный акк
        addField(settingsMap, SAVED_EMAIL_LIST, Utils.EMPTY);
        // social nets buttons settings. Stores value in form of JSON array. So default value is "[]"
        addField(settingsMap, SOCIAL_BUTTONS_SETTINGS, "[]");
        // преобразован старый конфиг в новый или нет
        addField(settingsMap, CONVERT_CONFIG, true);
        // time when popup about notification disabled is shown in the last
        addField(settingsMap, POPUP_NOTIFICATION_DISABLE_TIME, 0L);
        // social ids for social platforms obtained from server
        addField(settingsMap, DATA_APP_SOCIAL_IDS, "");
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
     * Api url for requests in ConnectionManager
     *
     * @return url for request
     */
    public String getApiDomain() {
        return getStringField(getSettingsMap(), DATA_API_URL);
    }

    /**
     * Scruffy Api url for requests in ConnectionManager
     *
     * @return scruffy url for request
     */
    public String getScruffyApiDomain() {
        return getStringField(getSettingsMap(), SCRUFFY_DATA_API_URL);
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
     * @param url путь к API
     */
    public void setApiUrl(String url) {
        SettingsMap settingsMap = getSettingsMap();
        setField(settingsMap, DATA_API_URL, url);
    }

    /**
     * Сохраняет все настройки, связаные с доступок к Scruffy API
     *
     * @param url путь к API
     */
    public void setScruffyApiUrl(String url) {
        SettingsMap settingsMap = getSettingsMap();
        setField(settingsMap, SCRUFFY_DATA_API_URL, url);
    }

    public void setStageLogin(String login, boolean checked) {
        SettingsMap settingsMap = getSettingsMap();
        setField(settingsMap, STAGE_LOGIN, login);
        setField(settingsMap, STAGE_CHECKED, checked);
    }

    public String getStageLogin() {
        return getStringField(getSettingsMap(), STAGE_LOGIN);
    }

    public boolean getStageChecked() {
        return getBooleanField(getSettingsMap(), STAGE_CHECKED);
    }

    /**
     * Url for api request with current saved version
     *
     * @return url for requests
     */
    public String getApiUrl() {
        SettingsMap settingsMap = getSettingsMap();
        return getStringField(settingsMap, DATA_API_URL) + "?v=" + ApiRequest.API_VERSION;
    }

    /**
     * Scruffy Url for api request with current saved version
     *
     * @return url for requests
     */
    public String getScruffyApiUrl() {
        SettingsMap settingsMap = getSettingsMap();
        return getStringField(settingsMap, SCRUFFY_DATA_API_URL) + "v" + ApiRequest.API_VERSION;
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

    /**
     * Last fullscreen ad show time
     *
     * @return last show time
     */
    public long getLastFullscreenTime() {
        return getLongField(getSettingsMap(), LAST_FULLSCREEN_TIME);
    }

    /**
     * Sets last fullscreen ad show time
     */
    public void setLastFullscreenTime(long time) {
        setField(getSettingsMap(), LAST_FULLSCREEN_TIME, time);
    }

    /**
     * Fullscreen ad url set
     *
     * @return url set
     */
    public Set<String> getFullscreenUrlsSet() {
        String urls = getStringField(getSettingsMap(), FULLSCREEN_URLS_SET);
        String[] urlList = TextUtils.split(urls, URL_SEPARATOR);
        return new HashSet<>(Arrays.asList(urlList));
    }

    public void setAdId(String adId) {
        setField(getSettingsMap(), AD_ID, adId);
    }

    public String getAdId() {
        return getStringField(getSettingsMap(), AD_ID);
    }

    /**
     * Adds url to fullscreen ad url set
     */
    public void addFullscreenUrl(String url) {
        String urls = getStringField(getSettingsMap(), FULLSCREEN_URLS_SET);
        setField(getSettingsMap(), FULLSCREEN_URLS_SET, urls.concat(URL_SEPARATOR).concat(url));
    }

    public int getDebugConnection() {
        return getIntegerField(getSettingsMap(), DEBUG_CONNECTION);
    }

    public void setDebugConnection(int type) {
        setField(getSettingsMap(), DEBUG_CONNECTION, type);
    }

    public boolean getDebugConnectionChecked() {
        return getBooleanField(getSettingsMap(), DEBUG_CONNECTION_CHECKED);
    }

    public void setDebugConnectionChecked(boolean checked) {
        setField(getSettingsMap(), DEBUG_CONNECTION_CHECKED, checked);
    }

    public int getLastAppVersion() {
        return getIntegerField(getSettingsMap(), LAST_APP_VERSION);
    }

    public void saveLastAppVersion() {
        setField(getSettingsMap(), LAST_APP_VERSION, BuildConfig.VERSION_CODE);
    }

    public long getTimeNotificationsDisabledShowAtLast() {
        return getLongField(getSettingsMap(), POPUP_NOTIFICATION_DISABLE_TIME);
    }

    public void setTimeNotificationsDisabledShowAtLast(long time) {
        setField(getSettingsMap(), POPUP_NOTIFICATION_DISABLE_TIME, time);
    }

    /**
     * Sets GCM registration id
     */
    public void setGcmRegId(String regId) {
        setField(getSettingsMap(), GCM_REG_ID, regId);
    }

    /**
     * GCM registration id
     *
     * @return gsm registration id
     */
    public String getGcmRegId() {
        return getStringField(getSettingsMap(), GCM_REG_ID);
    }

    /**
     * Sets list of all succesfull-auth emails
     *
     * @param savedEmailList list of emails
     */
    public void setSavedEmailList(String savedEmailList) {
        setField(getSettingsMap(), SAVED_EMAIL_LIST, savedEmailList);
    }

    public String getSavedEmailList() {
        return getStringField(getSettingsMap(), SAVED_EMAIL_LIST);
    }

    /**
     * Sets new settings for social net buttons on login screen
     */
    public void setSocialButtonsSettings(JSONArray socialButtonsSettings) {
        setField(getSettingsMap(), SOCIAL_BUTTONS_SETTINGS, socialButtonsSettings.toString());
    }

    /**
     * @return Social buttons settings
     */
    public JSONArray getSocialButtonsSettings() {
        try {
            return new JSONArray(getStringField(getSettingsMap(), SOCIAL_BUTTONS_SETTINGS));
        } catch (JSONException e) {
            throw new RuntimeException("Error getting social buttons settings", e);
        }
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

    public boolean isNeedConverting() {
        return getBooleanField(getSettingsMap(), CONVERT_CONFIG);
    }

    public boolean setUserConfigConverted() {
        return setField(getSettingsMap(), CONVERT_CONFIG, false);
    }


    public void resetAppSocialAppsIdsData() {
        resetAndSaveConfig(DATA_APP_OPTIONS);
    }

    public String getAppSocialAppsIds() {
        return getStringField(getSettingsMap(), DATA_APP_SOCIAL_IDS);
    }

    public void saveAppSocialAppsIds(AppSocialAppsIds appSocialAppsIds) {
        setField(getSettingsMap(), DATA_APP_SOCIAL_IDS, JsonUtils.toJson(appSocialAppsIds));
    }
}
