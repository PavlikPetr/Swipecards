package com.topface.topface.utils.config;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.App;
import com.topface.topface.data.AuthTokenStateData;
import com.topface.topface.utils.Utils;


/**
 * Created by ppavlik on 15.08.16.
 * Конфиг, который нигде не хранится, перезапуск приложения приведет к потере всех данных
 */

public class WeakStorage extends AbstractConfig {
    private static final String APPODEAL_BANNER_SEGMENT_NAME = "appodeal_banner_segment_name";
    private static final String APPODEAL_FULLSCREEN_SEGMENT_NAME = "appodeal_fullscreen_segment_name";
    private static final String PROFILE_DIALOG_REDESIGN_ENABLED = "profile_dialog_redesign_enabled";
    private static final String IS_TRANSLUCENT_DATING = "dating_redesign_enabled";
    private static final String IS_FIRST_SESSION = "is_first_session";
    private static final String IS_QUESTIONNAIRE_REQUEST_SENT = "is_questionnaire_request_sent";
    private static final String AUTH_TOKEN_STATE = "auth_token_state";

    public WeakStorage() {
        super(App.getContext());
    }

    @Override
    protected void fillSettingsMap(AbstractConfig.SettingsMap settingsMap) {
        // текущее имя сегмента для баннеров от аподил
        addField(settingsMap, APPODEAL_BANNER_SEGMENT_NAME, Utils.EMPTY);
        // текущее имя сегмента для фулскринов от аподил
        addField(settingsMap, APPODEAL_FULLSCREEN_SEGMENT_NAME, Utils.EMPTY);
        // строковая обертка над boolean чтобы знать что значение было установлено
        addField(settingsMap, PROFILE_DIALOG_REDESIGN_ENABLED, Utils.EMPTY);
        // строковая обертка над boolean чтобы знать что значение было установлено
        // если установлено, то отражает разрешение на показ редизайна знакомств
        addField(settingsMap, IS_TRANSLUCENT_DATING, Utils.EMPTY);
        // если это первая сессия после установки - true
        addField(settingsMap, IS_FIRST_SESSION, false);
        // признак того был ли отправлен запрос на получениенастроек для опросника
        addField(settingsMap, IS_QUESTIONNAIRE_REQUEST_SENT, false);
        // статус токена авторизации
        addField(settingsMap, AUTH_TOKEN_STATE, Utils.EMPTY);
    }

    @Override
    protected SharedPreferences getPreferences() {
        return null;
    }

    @Override
    protected void initData() {
        resetSettingsMap();
    }

    /**
     * Set name of appodeal banner segment
     *
     * @param name segment
     */
    public void setAppodealBannerSegmentName(String name) {
        setField(getSettingsMap(), APPODEAL_BANNER_SEGMENT_NAME, name);
    }

    /**
     * @return segment
     */
    public String getAppodealBannerSegmentName() {
        return getStringField(getSettingsMap(), APPODEAL_BANNER_SEGMENT_NAME);
    }

    /**
     * Set name of appodeal fullscreen segment
     *
     * @param name segment
     */
    public void setAppodealFullscreenSegmentName(String name) {
        setField(getSettingsMap(), APPODEAL_FULLSCREEN_SEGMENT_NAME, name);
    }

    /**
     * @return segment
     */
    public String getAppodealFullscreenSegmentName() {
        return getStringField(getSettingsMap(), APPODEAL_FULLSCREEN_SEGMENT_NAME);
    }

    /**
     * Resets stored "design version" for feeds
     */
    public void resetProfileDialogRedesignEnabled() {
        setField(getSettingsMap(), PROFILE_DIALOG_REDESIGN_ENABLED, Utils.EMPTY);
    }

    /**
     * @return true if must use new design for dating
     */
    public boolean getIsTranslucentDating() {
        SettingsMap settingsMap = getSettingsMap();
        if (TextUtils.isEmpty(getStringField(settingsMap, IS_TRANSLUCENT_DATING))) {
            setField(settingsMap, IS_TRANSLUCENT_DATING, String.valueOf(App.get().getOptions().isTranslucentDating()));
        }
        return Boolean.valueOf(getStringField(getSettingsMap(), IS_TRANSLUCENT_DATING));
    }

    /**
     * Resets stored "design version" for dating
     */
    public void resetIsTranslucentDating() {
        setField(getSettingsMap(), IS_TRANSLUCENT_DATING, Utils.EMPTY);
    }

    /**
     * Метод установки флага на текущую сессию
     * Если эта сессия первая после установки, не обновления, значит сетим true
     *
     * @param isFirst true if this session first after install app
     */
    public void setFirstSessionAfterInstallAttribute(boolean isFirst) {
        setField(getSettingsMap(), IS_FIRST_SESSION, isFirst);
    }

    /**
     * Признак того, что текущая сессия является первой после установки приложения
     */
    public boolean isFirstSessionAfterInstall() {
        return getBooleanField(getSettingsMap(), IS_FIRST_SESSION);
    }

    /**
     * Метода переводит в состояние true статус отправки запроса на получение настроек для опросника
     */
    public void questionnaireRequestSent() {
        setField(getSettingsMap(), IS_QUESTIONNAIRE_REQUEST_SENT, true);
    }

    /**
     * Получить текущий статус отправки запроса для опросника
     *
     * @return true если запрос в этой сессии уже был отправлен
     */
    public boolean isQuestionnaireRequestSent() {
        return getBooleanField(getSettingsMap(), IS_QUESTIONNAIRE_REQUEST_SENT);
    }

    /**
     * Set current state of auth token
     *
     * @param data current auth token state
     */
    public void setAuthTokenState(AuthTokenStateData data) {
        setField(getSettingsMap(), AUTH_TOKEN_STATE, JsonUtils.toJson(data));
    }

    /**
     * Get current state of auth token
     *
     * @return current auth token state
     */
    public AuthTokenStateData getAuthTokenState() {
        AuthTokenStateData state = JsonUtils.fromJson(getStringField(getSettingsMap(), AUTH_TOKEN_STATE), AuthTokenStateData.class);
        return state == null ? new AuthTokenStateData() : state;
    }
}
