package com.topface.topface.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.InstrumentationTestCase;

import com.topface.framework.utils.Debug;
import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.config.UserConfigConverter;

import java.util.Map;

/**
 * Тест для проверки класса UserConfigConverter
 * Created by onikitin on 15.01.15.
 */
public class UserConfigConverterTest extends InstrumentationTestCase {

    private int currentIteration = 0;

    public void testConfigManagerSeparateOldConfig() {
        OldPreferencesGenerator oldPreferencesGenerator = new OldPreferencesGenerator(App.getContext());
        oldPreferencesGenerator.commitConfig();
        UserConfigConverter configConverter = new UserConfigConverter("test@gmail.com",null);
        configConverter.getAllLogins();
        configConverter.separateConfig();

        Map<String, ?> oldFields = oldPreferencesGenerator.getFakePreferences().getAll();
        SharedPreferences[] preferenceses = oldPreferencesGenerator.getAllUniclePerfirences();
        String[] parts = oldPreferencesGenerator.getPrefParts();
        StringBuilder key = new StringBuilder();
        for (SharedPreferences preferences : preferenceses) {
            Map<String, ?> map = preferences.getAll();
            for (Map.Entry entry : map.entrySet()) {
                key.append(parts[currentIteration]).append("&").append(entry.getKey());

                Object o1 = entry.getValue();
                Object o2 = oldFields.get(key.toString());
                Debug.debug(o1, "assert o1=" + o1 + " o2= " + o2);

                assertEquals(o1, o2);
                key.setLength(0);
            }
            currentIteration++;
        }
        assertEquals(preferenceses.length, parts.length);
    }


    private static class OldPreferencesGenerator extends AbstractConfig {

        private String[] mPrefParts;

        public String[] getPrefParts() {
            return mPrefParts;
        }

        public OldPreferencesGenerator(Context context) {
            super(context);

        }

        private void addField(SettingsMap settingsMap, String key, Object defaultValue, String part) {
            addField(settingsMap, generateUniqueKey(key, part), defaultValue);
        }

        @Override
        protected void addField(SettingsMap settingsMap, String key, Object defaultValue) {
            super.addField(settingsMap, key, defaultValue);
        }

        @Override
        protected void fillSettingsMap(SettingsMap settingsMap) {
            mPrefParts = new String[]{"st&test@gmail.com", "vk&5465658", "ok&458756888"};
            for (String prefPart : mPrefParts) {
                // pincode value
                addField(settingsMap, UserConfig.DATA_PIN_CODE, Static.EMPTY, prefPart);
                // admirations promo popup last date of show
                addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_ADMIRATIONS), System.currentTimeMillis(), prefPart);
                // messages promo popup last date of show
                addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_MESSAGES), System.currentTimeMillis(), prefPart);
                // visitors promo popup last date of show
                addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_VISITORS), System.currentTimeMillis(), prefPart);
                // flag show if "buy sympathies hint" is passed
                addField(settingsMap, UserConfig.DATA_NOVICE_BUY_SYMPATHY, true, prefPart);
                // data of first launch to show "buy sympathies hint" with some delay from first launch
                addField(settingsMap, UserConfig.DATA_NOVICE_BUY_SYMPATHY_DATE, System.currentTimeMillis(), prefPart);
                // flag show if "send sympathy hint" is passed
                addField(settingsMap, UserConfig.DATA_NOVICE_SYMPATHY, true, prefPart);
                // список сообщений для сгруппированных нотификаций (сейчас группируются только сообщения)
                addField(settingsMap, UserConfig.NOTIFICATIONS_MESSAGES_STACK, Static.EMPTY, prefPart);
                // количество нотификаций, которые пишем в поле "еще %d сообщений"
                addField(settingsMap, UserConfig.NOTIFICATION_REST_MESSAGES, 0, prefPart);
                // время последнего сброса счетчика вкладки бонусов
                addField(settingsMap, UserConfig.DATA_BONUS_LAST_SHOW_TIME, System.currentTimeMillis(), prefPart);
                // default text for instant message on dating screen
                addField(settingsMap, UserConfig.DEFAULT_DATING_MESSAGE, Static.EMPTY, prefPart);
                // push notification melody
                addField(settingsMap, UserConfig.SETTINGS_GCM_RINGTONE, UserConfig.DEFAULT_SOUND, prefPart);
                // preload photo default type WiFi and 3G
                addField(settingsMap, UserConfig.SETTINGS_PRELOAD_PHOTO, PreloadPhotoSelectorTypes.WIFI_3G.getId(), prefPart);
                // is vibration for notification enabled
                addField(settingsMap, UserConfig.SETTINGS_GCM_VIBRATION, true, prefPart);
                // is led blinking for notification enabled
                addField(settingsMap, UserConfig.SETTINGS_GCM_LED, true, prefPart);
                // is push notification enabled or not
                addField(settingsMap, UserConfig.SETTINGS_GCM, true, prefPart);
                // purchased subscriptions which don't need verification
                addField(settingsMap, UserConfig.PURCHASED_SUBSCRIPTIONS, prefPart, prefPart);
                // время последнего показа попапа блокировки знакомств
                addField(settingsMap, UserConfig.DATING_LOCK_POPUP_TIME, System.currentTimeMillis(), prefPart);
                // счётчик перехода на экран офервола топфейс
                addField(settingsMap, UserConfig.TOPFACE_OFFERWALL_REDIRECT_COUNTER, 0, prefPart);
            }
        }

        @Override
        protected SharedPreferences getPreferences() {
            return getContext().getSharedPreferences(
                    UserConfig.PROFILE_CONFIG_SETTINGS,
                    Context.MODE_PRIVATE
            );
        }

        public SharedPreferences getFakePreferences() {
            return getPreferences();
        }

        protected String generateUniqueKey(String name, String part) {
            return part + Static.AMPERSAND + name;
        }

        private String getPromoPopupKey(int popupType) {
            return UserConfig.DATA_PROMO_POPUP + popupType;
        }

        /**
         * Возвращает массив разделенных конфигов
         */
        public SharedPreferences[] getAllUniclePerfirences() {
            SharedPreferences[] configs = new SharedPreferences[mPrefParts.length];
            int i = 0;
            for (SharedPreferences preferences : configs) {
                String s = mPrefParts[i].substring(mPrefParts[i].indexOf("&") + 1);
                preferences = getContext().getSharedPreferences(
                        UserConfig.PROFILE_CONFIG_SETTINGS + Static.AMPERSAND + s,
                        Context.MODE_PRIVATE);
                configs[i] = preferences;
                i++;
            }
            return configs;
        }
    }
}
