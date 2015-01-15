package com.topface.topface.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.InstrumentationTestCase;

import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.utils.config.ConfigConverter;
import com.topface.topface.utils.config.UserConfig;

import java.util.Map;

/**
 * Тест для проверки класса ConfigManager
 * Created by onikitin on 15.01.15.
 */
public class ConfigConverterTest extends InstrumentationTestCase {

    private int currentIteration = 0;

    public void testConfigManagerDivOldConfig() {
        App.getContext().getSharedPreferences(
                UserConfig.PROFILE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE).edit().clear().apply();
        OldPreferencesGenerator oldPreferencesGenerator = new OldPreferencesGenerator(App.getContext());
        oldPreferencesGenerator.waitRecord();
        ConfigConverter configConverter = new ConfigConverter("test@gmail.com");
        configConverter.divConfig();

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

                assertEquals(o1, o2);
                key.setLength(0);
            }
            currentIteration++;
        }
        assertEquals(preferenceses.length, parts.length);
    }


    private static class OldPreferencesGenerator extends AbstractConfig {

        /**
         * Keys' names to generate user-based keys
         */
        private static final String DATA_PROMO_POPUP = "data_promo_popup_";
        private static final String DATA_LIKE_CLOSING_LAST_TIME = "data_closings_likes_last_date";
        private static final String DATA_MUTUAL_CLOSING_LAST_TIME = "data_closings_mutual_last_date";
        private static final String DATA_BONUS_LAST_SHOW_TIME = "data_bonus_last_show_time";
        private static final String DEFAULT_DATING_MESSAGE = "default_dating_message";
        public static final String SETTINGS_GCM_RINGTONE = "settings_c2dm_ringtone";
        private int mCurrentIteration = 0;

        public String[] getPrefParts() {
            return mPrefParts;
        }

        String[] mPrefParts;

        public OldPreferencesGenerator(Context context) {
            super(context);
        }

        @Override
        protected void addField(SettingsMap settingsMap, String key, Object defaultValue) {
            super.addField(settingsMap, generateUniqueKey(key), defaultValue);
        }

        @Override
        protected void fillSettingsMap(SettingsMap settingsMap) {
            String[] prefParts = {"st&test@gmail.com", "vk&5465658", "ok&458756888"};
            mPrefParts = prefParts;
            removeFakePreferences();
            for (int i = 0; i < prefParts.length; i++) {
                // pincode value
                addField(settingsMap, UserConfig.DATA_PIN_CODE, Static.EMPTY);
                // admirations promo popup last date of show
                addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_ADMIRATIONS), System.currentTimeMillis());
                // messages promo popup last date of show
                addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_MESSAGES), System.currentTimeMillis());
                // visitors promo popup last date of show
                addField(settingsMap, getPromoPopupKey(Options.PromoPopupEntity.AIR_VISITORS), System.currentTimeMillis());
                // flag show if "buy sympathies hint" is passed
                addField(settingsMap, UserConfig.DATA_NOVICE_BUY_SYMPATHY, true);
                // data of first launch to show "buy sympathies hint" with some delay from first launch
                addField(settingsMap, UserConfig.DATA_NOVICE_BUY_SYMPATHY_DATE, System.currentTimeMillis());
                // flag show if "send sympathy hint" is passed
                addField(settingsMap, UserConfig.DATA_NOVICE_SYMPATHY, true);
                // date of last likes closings processing
                addField(settingsMap, DATA_LIKE_CLOSING_LAST_TIME, System.currentTimeMillis());
                // date of last mutual closings processing
                addField(settingsMap, DATA_MUTUAL_CLOSING_LAST_TIME, System.currentTimeMillis());
                // список сообщений для сгруппированных нотификаций (сейчас группируются только сообщения)
                addField(settingsMap, UserConfig.NOTIFICATIONS_MESSAGES_STACK, Static.EMPTY);
                // количество нотификаций, которые пишем в поле "еще %d сообщений"
                addField(settingsMap, UserConfig.NOTIFICATION_REST_MESSAGES, 0);
                // время последнего сброса счетчика вкладки бонусов
                addField(settingsMap, DATA_BONUS_LAST_SHOW_TIME, System.currentTimeMillis());
                // default text for instant message on dating screen
                addField(settingsMap, DEFAULT_DATING_MESSAGE, Static.EMPTY);
                // push notification melody
                addField(settingsMap, SETTINGS_GCM_RINGTONE, UserConfig.DEFAULT_SOUND);
                // is vibration for notification enabled
                addField(settingsMap, UserConfig.SETTINGS_GCM_VIBRATION, true);
                // is led blinking for notification enabled
                addField(settingsMap, UserConfig.SETTINGS_GCM_LED, true);
                // is push notification enabled or not
                addField(settingsMap, UserConfig.SETTINGS_GCM, true);
                // purchased subscriptions which don't need verification
                addField(settingsMap, UserConfig.PURCHASED_SUBSCRIPTIONS, mPrefParts[mCurrentIteration]);
                // время последнего показа попапа блокировки знакомств
                addField(settingsMap, UserConfig.DATING_LOCK_POPUP_TIME, System.currentTimeMillis());
                // счётчик перехода на экран офервола топфейс
                addField(settingsMap, UserConfig.TOPFACE_OFFERWALL_REDIRECT_COUNTER, 0);
                mCurrentIteration++;
            }
            saveConfig();
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

        protected String generateUniqueKey(String name) {
            return mPrefParts[mCurrentIteration] + Static.AMPERSAND + name;
        }

        private String getPromoPopupKey(int popupType) {
            return DATA_PROMO_POPUP + popupType;
        }

        public void removeFakePreferences() {
            getPreferences().edit().clear().apply();
        }

        /**
         * Возвращает массив разделенных конфигов
         */
        public SharedPreferences[] getAllUniclePerfirences() {
            waitRecord();
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

        /**
         * Запись конфига идет в отдельном потоку без ожидания рискуем получить NPE при запросе конфига
         */
        public void waitRecord() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
