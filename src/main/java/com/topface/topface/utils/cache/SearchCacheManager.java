package com.topface.topface.utils.cache;

import android.text.TextUtils;

import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

import org.json.JSONObject;

/**
 * Кэш для поиска
 */
public class SearchCacheManager extends PreferencesCacheManager {
    private static final String CACHE_KEY = "Search";
    private static final int LIFE_TIME = 7200; //2 часа: 2 * 60 * 60
    private static final String CACHE_KEY_SEARCH_POSITION_POSTFIX = "_search_position";
    private static final String CACHE_KEY_SEARCH_SIGNATURE_POSTFIX = "_search_signature";

    public SearchCacheManager() {
        super();
    }

    public void setCache(final UsersList usersList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getEditor()
                            .putString(getDataCacheKey(CACHE_KEY), usersList.toJson().toString())
                            .putLong(getExpireDateCacheKey(CACHE_KEY), getExpireDateTimestamp(LIFE_TIME))
                            .putInt(getSearchPositionCacheKey(CACHE_KEY), usersList.getSearchPosition())
                            .putString(getSearchSignatureCacheKey(CACHE_KEY), usersList.getSignature())
                            .commit();
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        }).start();
    }

    public void saveSearchSignature(String searchSignature) {
        UsersList.log("save signature to cache " + searchSignature);
        getEditor().putString(getSearchSignatureCacheKey(CACHE_KEY), searchSignature).commit();
    }

    public UsersList getCache() {
        return parseCacheData(super.getCache(CACHE_KEY));
    }

    @SuppressWarnings("unchecked")
    private UsersList parseCacheData(String cache) {
        UsersList usersList = null;

        if (cache != null) {
            try {
                usersList = new UsersList(new JSONObject(cache), SearchUser.class);
                usersList.setSearchPosition(getSearchPosition());
                usersList.updateSignature();
            } catch (Exception e) {
                Debug.error(e);
            }
        }

        return usersList;
    }

    public int getSearchPosition() {
        return mPreferences.getInt(getSearchPositionCacheKey(CACHE_KEY), 0);
    }

    public void clearCache() {
        getEditor()
                .remove(getSearchPositionCacheKey(CACHE_KEY))
                .remove(getDataCacheKey(CACHE_KEY))
                .remove(getExpireDateCacheKey(CACHE_KEY))
                .commit();
    }

    protected String getSearchPositionCacheKey(String cacheKey) {
        return CACHE_KEY_PREFIX + cacheKey + CACHE_KEY_SEARCH_POSITION_POSTFIX;
    }

    protected String getSearchSignatureCacheKey(String cacheKey) {
        return CACHE_KEY_PREFIX + cacheKey + CACHE_KEY_SEARCH_SIGNATURE_POSTFIX;
    }

    @Override
    protected boolean isCacheExpired(String cacheKey) {
        return CacheProfile.dating == null || super.isCacheExpired(cacheKey) || //Проверяем соответсвие кэша текущему фильтру поиска
                !TextUtils.equals(getSignatureFromCache(), CacheProfile.dating.getFilterSignature());
    }

    protected String getSignatureFromCache() {
        return mPreferences.getString(getSearchSignatureCacheKey(CACHE_KEY), null);
    }
}
