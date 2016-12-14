package com.topface.topface.utils.config;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.App;
import com.topface.topface.utils.Utils;


/**
 * Created by ppavlik on 15.08.16.
 * Конфиг, который нигде не хранится, перезапуск приложения приведет к потере всех данных
 */

public class WeakStorage extends AbstractConfig {
    private static final String APPODEAL_BANNER_SEGMENT_NAME = "appodeal_banner_segment_name";
    private static final String APPODEAL_FULLSCREEN_SEGMENT_NAME = "appodeal_fullscreen_segment_name";
    private static final String PROFILE_DIALOG_REDESIGN_ENABLED = "profile_dialog_redesign_enabled";

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
     * @return true if must use new design for feeds
     */
    public boolean getProfileDialogRedesignEnabled() {
        SettingsMap settingsMap = getSettingsMap();
        if (TextUtils.isEmpty(getStringField(settingsMap, PROFILE_DIALOG_REDESIGN_ENABLED))) {
            setField(settingsMap, PROFILE_DIALOG_REDESIGN_ENABLED, String.valueOf(App.get().getOptions().getDialogRedesignEnabled()));
        }
        return Boolean.valueOf(getStringField(getSettingsMap(), PROFILE_DIALOG_REDESIGN_ENABLED));
    }

    /**
     * Resets stored "design version" for feeds
     */
    public void resetProfileDialogRedesignEnabled() {
        setField(getSettingsMap(), PROFILE_DIALOG_REDESIGN_ENABLED, Utils.EMPTY);
    }
}
