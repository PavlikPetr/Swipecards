package com.topface.topface.ui;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import com.topface.topface.R;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.TrackedPreferenceActivity;

/**
 *  Базовая версия экрана настроек. Настройка уведомлений и возможность выключить предзагрузку.
 *  Есть возможность поменять тип предзагрузки, но она пока не поддерживается приложением, поэтому закоментирована
 */
public class SettingsActivity extends TrackedPreferenceActivity {

    private Preference mVibration;
    private Preference mRingtone;
    //private ListPreference mPreloadingType;
    /*private Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String value = (String) o;
            String offValue = getString(R.string.settings_preloading_off);
            checkPreloadingStatus(
                    !value.equals(offValue)
            );
            return true;
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mRingtone = findPreference(Settings.SETTINGS_C2DM_RINGTONE);
        mVibration = findPreference(Settings.SETTINGS_C2DM_VIBRATION);

        findPreference("settings_c2dm").setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        checkNotificationStatus((Boolean) o);
                        return true;
                    }
                }
        );

        ListPreference preloading = (ListPreference) findPreference(Settings.SETTINGS_PRELOADING);
        preloading.setEntries(R.array.settings_preloading_entries);
        preloading.setEntryValues(R.array.settings_preloading_values);
        preloading.setDefaultValue(Settings.getInstance().getPreloading());
        //preloading.setOnPreferenceChangeListener(onPreferenceChangeListener);

        /*mPreloadingType = (ListPreference)findPreference(Settings.SETTINGS_PRELOADING_TYPE);
        mPreloadingType.setEntries(R.array.settings_preloading_type_entries);
        mPreloadingType.setEntryValues(R.array.settings_preloading_type_values);
        mPreloadingType.setDefaultValue(Settings.getInstance().getPreloadingType());*/

        checkNotificationStatus(Settings.getInstance().isNotificationEnabled());
        //checkPreloadingStatus(!Settings.getInstance().isPreloadingDisabled());
    }

    private void checkNotificationStatus(boolean isEnabled) {
        if (isEnabled) {
            mRingtone.setEnabled(true);
            mVibration.setEnabled(true);
        }
        else {
            mRingtone.setEnabled(false);
            mVibration.setEnabled(false);
        }
    }

    /*private void checkPreloadingStatus(boolean isEnabled) {
        if (isEnabled) {
            mPreloadingType.setEnabled(true);
        }
        else {
            mPreloadingType.setEnabled(false);
        }
    }*/

}
