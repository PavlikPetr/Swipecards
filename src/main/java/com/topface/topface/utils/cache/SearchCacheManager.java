package com.topface.topface.utils.cache;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONException;

/**
 * Кэш для поиска
 */
public class SearchCacheManager extends UsersListCacheManager {
    private static final String CACHE_KEY = "Search";
    private static final int LIFE_TIME = 7200; //2 часа: 2 * 60 * 60
    private static final String CACHE_KEY_SEARCH_SIGNATURE_POSTFIX = "_search_signature";

    public SearchCacheManager() {
        super(CACHE_KEY, SearchUser.class);
    }

    @Override
    protected void saveToCache(SharedPreferences.Editor editor, UsersList usersList) throws JSONException {
        super.saveToCache(editor, usersList);
        editor.putLong(getExpireDateCacheKey(CACHE_KEY), getExpireDateTimestamp(LIFE_TIME));
        editor.putString(getSearchSignatureCacheKey(CACHE_KEY), usersList.getSignature());
    }

    public void saveSearchSignature(String searchSignature) {
        UsersList.log("save signature to cache " + searchSignature);
        getEditor().putString(getSearchSignatureCacheKey(CACHE_KEY), searchSignature).commit();
    }

    public void clearCache() {
        getEditor()
                .remove(getDataCacheKey(CACHE_KEY))
                .remove(getExpireDateCacheKey(CACHE_KEY))
                .remove(getPositionCacheKey(CACHE_KEY))
                .remove(getSearchSignatureCacheKey(CACHE_KEY))
                .commit();
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
