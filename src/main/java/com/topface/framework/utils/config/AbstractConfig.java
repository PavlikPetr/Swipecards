package com.topface.framework.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kirussell on 06.01.14.
 * Extend this class to store different types of data in shared preferences
 * Implement fillSettingsMap() to add fields and default values for extended config
 * All data is read from shared preferences if exists than stores in SettingsMap object
 * Use saveConfig() method to write it in shared preferences (on disk)
 */
public abstract class AbstractConfig {

    private Context mContext;

    private SettingsMap mSettingsMap;

    public AbstractConfig(Context context) {
        mContext = context;
        initData();
    }

    /**
     * Получаем значения полей из SharedPreferences
     */
    protected final void initData() {
        if (canInitData()) {
            SharedPreferences preferences = getPreferences();
            for (SettingsField field : getSettingsMap(true).values()) {
                switch (field.getType()) {
                    case String:
                        field.value = preferences.getString(field.key, (String) field.value);
                        break;
                    case Integer:
                        field.value = preferences.getInt(field.key, (Integer) field.value);
                        break;
                    case Boolean:
                        field.value = preferences.getBoolean(field.key, (Boolean) field.value);
                        break;
                    case Long:
                        field.value = preferences.getLong(field.key, (Long) field.value);
                        break;
                }
            }
        }
    }

    /**
     * Condition in which initial data can be read to config from preferences
     *
     * @return true as default
     */
    protected boolean canInitData() {
        return true;
    }

    protected Context getContext() {
        return mContext;
    }

    protected SettingsMap getSettingsMap() {
        return getSettingsMap(false);
    }

    private SettingsMap getSettingsMap(boolean onlyNew) {
        if (mSettingsMap == null || onlyNew) {
            mSettingsMap = newSettingsMap();
        }
        return mSettingsMap;
    }

    private SettingsMap newSettingsMap() {
        SettingsMap settingsMap = new SettingsMap();
        fillSettingsMap(settingsMap);
        return settingsMap;
    }

    /**
     * Add fields and default values for current config
     * Add fields with {@link #addField(AbstractConfig.SettingsMap, String, Object)}
     *
     * @param settingsMap settingsMap that stores config data
     */
    protected abstract void fillSettingsMap(SettingsMap settingsMap);

    /**
     * Use to populate settings map
     *
     * @param settingsMap  map to populate
     * @param key          field's key
     * @param defaultValue field's default value
     */
    protected void addField(SettingsMap settingsMap, String key, Object defaultValue) {
        settingsMap.addField(key, defaultValue);
    }

    protected boolean setField(SettingsMap settingsMap, String key, Object defaultValue) {
        return settingsMap.setField(key, defaultValue);
    }

    protected String getStringField(SettingsMap settingsMap, String key) {
        return settingsMap.getStringField(key);
    }

    protected int getIntegerField(SettingsMap settingsMap, String key) {
        return settingsMap.getIntegerField(key);
    }

    protected boolean getBooleanField(SettingsMap settingsMap, String key) {
        return settingsMap.getBooleanField(key);
    }

    protected Long getLongField(SettingsMap settingsMap, String key) {
        return settingsMap.getLongField(key);
    }

    protected Double getDoubleField(SettingsMap settingsMap, String key) {
        return settingsMap.getDoubleField(key);
    }

    /**
     * Sets default data
     */
    protected void resetSettingsMap() {
        mSettingsMap = newSettingsMap();
    }

    /**
     * Sets default data for concrete field
     */
    protected void resetSettingsMap(String key) {
        SettingsField field = getSettingsMap().get(key);
        if (field != null) {
            field.resetToDefault();
        }
    }

    /**
     * Sets default data and saves it
     */
    public void resetAndSaveConfig() {
        //Возвращаем значения по умолчанию
        resetSettingsMap();
        //Сохраняем default значения
        saveConfig();
        Debug.log("Reset AppConfig: " + toString());
    }

    /**
     * Sets default data for concrete field and saves it
     */
    protected void resetAndSaveConfig(String key) {
        //Возвращаем значения по умолчанию
        resetSettingsMap(key);
        //Сохраняем default значения
        saveConfig();
        Debug.log("Reset AppConfig: " + toString());
    }

    protected abstract SharedPreferences getPreferences();

    /**
     * Сохранет текущее состояние конфига, просто записывая данные из памяти
     * Note: Saves data asynchronously in background thread
     */
    public final void saveConfig() {
        new BackgroundThread() {
            @Override
            public void execute() {
                SharedPreferences.Editor editor = getPreferences().edit();
                saveConfigAdditional(editor);
                for (SettingsField field : getSettingsMap().values()) {
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
                }
                editor.commit();
                Debug.log(this.getClass().getName() + toString());
            }
        };
    }

    /**
     * Override to save additional data
     * Method is called when saveConfig obtain editor object
     * Note: called in background thread
     *
     * @param editor object from sharedPreferences to save additional data
     */
    protected void saveConfigAdditional(SharedPreferences.Editor editor) {
    }

    /**
     * Types for SettingsField
     */
    public static enum FieldType {
        String, Integer, Boolean, Long, List, Unknown
    }

    /**
     * Поле настроек конфига. Нужно для того, что бы их было легко добавлять поля настроек
     */
    private static class SettingsField<T> {
        public SettingsField(String key, T defaultValue) {
            this.key = key;
            this.value = defaultValue;
            this.defaultValue = defaultValue;
        }

        public String key;
        public T value;
        public T defaultValue;

        public FieldType getType() {
            if (value instanceof String) {
                return FieldType.String;
            } else if (value instanceof Integer) {
                return FieldType.Integer;
            } else if (value instanceof Boolean) {
                return FieldType.Boolean;
            } else if (value instanceof Long) {
                return FieldType.Long;
            } else if (value instanceof List) {
                return FieldType.List;
            }
            return FieldType.Unknown;
        }

        public void resetToDefault() {
            this.value = this.defaultValue;
        }

        @Override
        public String toString() {
            return key + " : " + value + "(" + defaultValue + ")";
        }
    }

    /**
     * Класс хранения полей настроек
     */
    protected static class SettingsMap extends HashMap<String, SettingsField> {

        private SettingsField addField(String fieldName, Object defaultValue) {
            return put(fieldName, new SettingsField<>(fieldName, defaultValue));
        }

        private boolean setField(String fieldName, Object value) {
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
                return true;
            }
            return false;
        }

        private String getStringField(String fieldName) {
            SettingsField settingsField = get(fieldName);
            if (settingsField != null && settingsField.value != null) {
                return (String) settingsField.value;
            }
            return "";
        }

        private int getIntegerField(String fieldName) {
            SettingsField settingsField = get(fieldName);
            if (settingsField != null && settingsField.value != null) {
                return (Integer) settingsField.value;
            }
            return 0;
        }

        private boolean getBooleanField(String fieldName) {
            SettingsField settingsField = get(fieldName);
            if (settingsField != null && settingsField.value != null) {
                return (Boolean) settingsField.value;
            }
            return false;
        }

        private Long getLongField(String fieldName) {
            SettingsField settingsField = get(fieldName);
            if (settingsField != null && settingsField.value != null) {
                return (Long) settingsField.value;
            }
            return 0L;
        }

        private Double getDoubleField(String fieldName) {
            SettingsField settingsField = get(fieldName);
            if (settingsField != null && settingsField.value != null) {
                return (Double) settingsField.value;
            }
            return 0.0;
        }
    }
}
