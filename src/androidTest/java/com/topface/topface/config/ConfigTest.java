package com.topface.topface.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.InstrumentationTestCase;

import com.topface.framework.utils.config.AbstractConfig;
import com.topface.topface.App;

/**
 * Created by kirussell on 19.05.2014.
 * Tests for AbstractConfig
 * - test added fields with defaultValues
 */
public class ConfigTest extends InstrumentationTestCase {
    private static final String testStringKey = "keyString";
    private static final String testStringValue = "valueString";

    private static final String testLongKey = "keyLong";
    private static final Long testLongValue = 10L;

    public void testAbstractConfigAddedFieldsDefaultValues() {
        DummyConfig config = new DummyConfig(App.getContext());
        assertTrue(config.getStringField(testStringKey) instanceof String);
        assertEquals(config.getStringField(testStringKey), testStringValue);

        assertTrue(config.getLongField(testLongKey) instanceof Long);
        assertEquals(config.getLongField(testLongKey), testLongValue);
    }

    private class DummyConfig extends AbstractConfig {

        public DummyConfig(Context context) {
            super(context);
        }

        @Override
        protected void fillSettingsMap(SettingsMap settingsMap) {
            addField(settingsMap, testStringKey, testStringValue);
            addField(settingsMap, testLongKey, testLongValue);
        }

        @Override
        protected SharedPreferences getPreferences() {
            return getContext().getSharedPreferences("testSharedPrefs", Context.MODE_PRIVATE);
        }

        public Object getStringField(String key) {
            return getStringField(getSettingsMap(), key);
        }

        public Object getLongField(String key) {
            return getLongField(getSettingsMap(), key);
        }
    }
}
