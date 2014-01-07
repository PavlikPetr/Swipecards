package com.topface.topface.utils.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.topface.topface.utils.Debug;

import java.util.HashMap;

/**
 * Created by kirussell on 06.01.14.
 * Extend this class to store different type of data in shared preferences
 * All data is read from sharedPreferences if exists than stores in SettingsMap object
 * It can be changed and then saved with saveConfig() method
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
    private void initData() {
        if (canInitData()) {
            SharedPreferences preferences = getPreferences();
            for (SettingsField field : getSettingsMap().values()) {
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
        if (mSettingsMap == null) {
            mSettingsMap = newSettingsMap();
        }
        return mSettingsMap;
    }

    protected void resetSettingsMap() {
        mSettingsMap = null;
    }

    protected abstract SettingsMap newSettingsMap();

    abstract SharedPreferences getPreferences();

    /**
     * Сохранет текущее состояние конфига, просто записывая данные из памяти
     */
    public final void saveConfig() {
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
                case Long:
                    editor.putLong(field.key, (Long) field.value);
                    break;
            }
        }
        editor.commit();
        Debug.log(this.getClass().getName() + toString());
    }

    /**
     * Override to save additional data
     * Method is called when saveConfig obtain editor object
     *
     * @param editor object from sharedPreferences to save additional data
     */
    protected void saveConfigAdditional(SharedPreferences.Editor editor) {
    }

    /**
     * Types for SettingsField
     */
    public static enum FieldType {
        String, Integer, Boolean, Long, Unknown
    }

    /**
     * Поле настроек конфига. Нужно для того, что бы их было легко добавлять поля настроек
     */
    protected static class SettingsField<T> {
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
    protected static class SettingsMap extends HashMap<String, SettingsField> {
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
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
                return true;
            }
            return false;
        }

        public boolean setField(String fieldName, Integer value) {
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
                return true;
            }
            return false;
        }

        @SuppressWarnings("UnusedDeclaration")
        public boolean setField(String fieldName, Long value) {
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
                return true;
            }
            return false;
        }

        @SuppressWarnings("UnusedDeclaration")
        public boolean setField(String fieldName, Boolean value) {
            if (containsKey(fieldName)) {
                get(fieldName).value = value;
                return true;
            }
            return false;
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

        @SuppressWarnings("UnusedDeclaration")
        public Long getLongField(String fieldName) {
            Long result = 0l;
            SettingsField settingsField = get(fieldName);
            if (settingsField != null) {
                result = (Long) settingsField.value;
            }
            return result;
        }
    }
}
