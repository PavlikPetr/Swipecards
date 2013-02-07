package com.topface.topface.utils.cache;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.topface.topface.App;

/**
 * Менеджер кеширования данных, хранящий данные в DefaultSharedPreferences
 */
public class PreferencesCacheManager extends AbstractCacheManager {
    private static PreferencesCacheManager mInstance;
    protected final SharedPreferences mPreferences;

    public PreferencesCacheManager() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }

    protected SharedPreferences.Editor getEditor() {
        return mPreferences.edit();
    }

    protected void setCache(String cacheKey, String data, int lifeTimeInSeconds) {
        getEditor()
                .putString(getDataCacheKey(cacheKey), data)
                .putLong(getExpireDateCacheKey(cacheKey), getExpireDateTimestamp(lifeTimeInSeconds))
                .commit();
    }

    protected String getDataFromCache(String cacheKey) {
        return mPreferences.getString(getDataCacheKey(cacheKey), null);
    }

    protected long getExpireDateFromCache(String cacheKey) {
        return mPreferences.getLong(getExpireDateCacheKey(cacheKey), 0);
    }

}
