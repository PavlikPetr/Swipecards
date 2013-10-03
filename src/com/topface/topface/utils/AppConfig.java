package com.topface.topface.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.topface.Static;

import java.util.HashMap;

/**
 * Класс для хранения в SharedPreferences тех настроек, которые обязательны для работы приложения,
 * но которые можно изменить в рантайме. Нужно прежде всего для редакторских функций
 */
public class AppConfig {

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

    private BannersConfig mBannerConfig;
    private LocaleConfig mLocaleConfig;

    public BannersConfig getBannerConfig() {
        return mBannerConfig;
    }

    public LocaleConfig getLocaleConfig() {
        return mLocaleConfig;
    }

    public String getApiRevision() {
        return mFields.getStringField(DATA_API_REVISION);
    }

    public String getAuthVkApi() {
        return mFields.getStringField(DATA_AUTH_VK_API);
    }

    public String getAuthFbApi() {
        return mFields.getStringField(DATA_AUTH_FB_API);
    }

    public void setDebugMode(int position) {
        mFields.setField(DATA_DEBUG_MODE, position);
    }

    public int getDebugMode() {
        return mFields.getIntegerField(DATA_DEBUG_MODE);
    }

    public void setEditorMode(int position) {
        mFields.setField(DATA_EDITOR_MODE, position);
    }

    public int getEditorMode() {
        return mFields.getIntegerField(DATA_EDITOR_MODE);
    }

    public Integer getApiVersion() {
        return mFields.getIntegerField(DATA_API_VERSION);
    }

    public String getApiRevisin() {
        return mFields.getStringField(DATA_API_REVISION);
    }

    public String getApiDomain() {
        return mFields.getStringField(DATA_API_URL);
    }

    public void setFloodEndsTime(long timestamp) {
        mFields.setField(FLOOD_ENDS_TIME, timestamp);
    }

    public long getFloodEndsTime() {
        return mFields.getLongField(FLOOD_ENDS_TIME);
    }


    /**
     * Возможные типы полей настроек
     * Поле с типом Unknown не будет обрабатываться (возникает, если поле имеет неизвестный класс у данных)
     */
    public static enum FieldType {
        String, Integer, Boolean, Long, Unknown
    }

    private Context mContext;

    /**
     * Список наших настроек
     */
    private static SettingsMap mFields = getNewFieldsMap();

    private static SettingsMap getNewFieldsMap() {
        SettingsMap fields = new SettingsMap();
        fields.addStringField(DATA_API_URL, Static.API_URL);
        fields.addIntegerField(DATA_API_VERSION, Static.API_VERSION);
        fields.addStringField(DATA_API_REVISION, null);
        fields.addStringField(DATA_AUTH_VK_API, Static.AUTH_VK_ID);
        fields.addStringField(DATA_AUTH_FB_API, Static.AUTH_FACEBOOK_ID);
        fields.addIntegerField(DATA_EDITOR_MODE, Editor.MODE_USER_FIELD);
        fields.addIntegerField(DATA_DEBUG_MODE, Debug.MODE_EDITOR);
        fields.addLongField(FLOOD_ENDS_TIME, 0l);
        return fields;
    }

    public AppConfig(Context context) {
        mContext = context;
        mBannerConfig = new BannersConfig(mContext);
        mLocaleConfig = new LocaleConfig(mContext);
        initData();
    }

    /**
     * Получаем значения полей из SharedPreferences
     */
    private void initData() {
        SharedPreferences preferences = getPreferences();

        //Данные из конфига читаем только если совпадает версия конфига
        if (preferences.getInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION) == APP_CONFIG_VERSION) {
            for (SettingsField field : mFields.values()) {
                switch (field.getType()) {
                    case String:
                        field.value = preferences.getString(field.key, (String) field.value);
                        break;
                    case Integer:
                        field.value = preferences.getInt(field.key, (Integer) field.value);
                        break;
                    case Boolean:
                        field.value = preferences.getBoolean(field.key, (Boolean) field.value);
                    case Long:
                        field.value = preferences.getLong(field.key, (Long) field.value);
                        break;
                }
            }
        }
    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(
                BASE_CONFIG_SETTINGS,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Сохранет текущее состояние конфига, просто записывая данные из памяти
     */
    public void saveConfig() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION);
        for (SettingsField field : mFields.values()) {
            switch (field.getType()) {
                case String:
                    editor.putString(field.key, (String) field.value);
                    break;
                case Integer:
                    editor.putInt(field.key, (Integer) field.value);
                    break;
                case Boolean:
                    editor.putBoolean(field.key, (Boolean) field.value);
                case Long:
                    editor.putLong(field.key, (Long) field.value);
                    break;
            }
        }
        editor.commit();
        Debug.log("Save AppConfig" + toString());
    }

    public void saveConfigField(String key) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION);
        SettingsField field = mFields.get(key);
        switch (field.getType()) {
            case String:
                editor.putString(field.key, (String) field.value);
                break;
            case Integer:
                editor.putInt(field.key, (Integer) field.value);
                break;
            case Boolean:
                editor.putBoolean(field.key, (Boolean) field.value);
            case Long:
                editor.putLong(field.key, (Long) field.value);
                break;
        }
        editor.commit();
        Debug.log("Save AppConfig" + toString());
    }

    /**
     * Удаляет все настройки из SharedPreferences, дабы применились настройки по умолчанию
     */
    public void resetToDefault() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(DATA_APP_CONFIG_VERSION, APP_CONFIG_VERSION);
        for (SettingsField field : mFields.values()) {
            editor.remove(field.key);
        }
        editor.commit();
        //Возвращаем значения по умолчанию
        mFields = getNewFieldsMap();
        Debug.log("Reset AppConfig: " + toString());
    }

    /**
     * Поле настроек конфига. Нужно для того, что бы их было легко добавлять поля настроек
     */
    private static class SettingsField<T> {
        public SettingsField(String key, T defaultValue) {
            this.key = key;
            this.value = defaultValue;
        }

        public String key;
        public T value;

        public FieldType getType() {
            if (value instanceof String) {
                return FieldType.String;
            } else if (value instanceof Integer) {
                return FieldType.Integer;
            } else if (value instanceof Boolean) {
                return FieldType.Boolean;
            } else if (value instanceof Long) {
                return FieldType.Long;
            }
            return FieldType.Unknown;
        }
    }

    /**
     * Класс хранения полей настроек
     */
    public static class SettingsMap extends HashMap<String, SettingsField> {
        @SuppressWarnings("unchecked")
        public SettingsField<String> addStringField(String fieldName, String defaultValue) {
            return put(fieldName, new SettingsField<String>(fieldName, defaultValue));
        }

        @SuppressWarnings("unchecked")
        public SettingsField<Integer> addIntegerField(String fieldName, Integer defaultValue) {
            return put(fieldName, new SettingsField<Integer>(fieldName, defaultValue));
        }

        @SuppressWarnings({"unchecked", "UnusedDeclaration"})
        public SettingsField<Integer> addBooleanField(String fieldName, Boolean defaultValue) {
            return put(fieldName, new SettingsField<Boolean>(fieldName, defaultValue));
        }

        @SuppressWarnings("unchecked")
        public SettingsField<Long> addLongField(String fieldName, Long defaultValue) {
            return put(fieldName, new SettingsField<Long>(fieldName, defaultValue));
        }

        public boolean setField(String fieldName, String value) {
            boolean result = false;
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
            }
            return result;
        }

        public boolean setField(String fieldName, Integer value) {
            boolean result = false;
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
            }
            return result;
        }

        public boolean setField(String fieldName, Long value) {
            boolean result = false;
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
            }
            return result;
        }

        @SuppressWarnings("UnusedDeclaration")
        public boolean setField(String fieldName, Boolean value) {
            boolean result = false;
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
            }
            return result;
        }

        public String getStringField(String fieldName) {
            String result = null;
            SettingsField settingsField = get(fieldName);
            if (settingsField != null) {
                result = (String) settingsField.value;
            }
            return result;
        }

        public Integer getIntegerField(String fieldName) {
            Integer result = null;
            SettingsField settingsField = get(fieldName);
            if (settingsField != null) {
                result = (Integer) settingsField.value;
            }
            return result;
        }

        @SuppressWarnings("UnusedDeclaration")
        public Boolean getBooleanField(String fieldName) {
            Boolean result = null;
            SettingsField settingsField = get(fieldName);
            if (settingsField != null) {
                result = (Boolean) settingsField.value;
            }
            return result;
        }

        public Long getLongField(String fieldName) {
            Long result = 0l;
            SettingsField settingsField = get(fieldName);
            if (settingsField != null) {
                result = (Long) settingsField.value;
            }
            return result;
        }
    }

    /**
     * Сохраняет все настройки, связаные с доступок к API
     *
     * @param url      путь к API
     * @param version  Версия API
     * @param revision Ревизия (будет работать только для тестовых платформ)
     */
    public void setApiUrl(String url, Integer version, String revision) {
        mFields.setField(DATA_API_URL, url);
        mFields.setField(DATA_API_VERSION, version);
        mFields.setField(DATA_API_REVISION, revision);
    }

    public String getApiUrl() {
        return mFields.getStringField(DATA_API_URL) + "?v=" + mFields.getIntegerField(DATA_API_VERSION);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append("\n").append("Version: ").append(APP_CONFIG_VERSION).append("\n");
        for (SettingsField field : mFields.values()) {
            result.append(field.key).append(": ").append(field.value).append("\n");
        }
        return result.toString();
    }
}
