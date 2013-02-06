package com.topface.topface.utils.cache;

import android.text.TextUtils;
import com.topface.topface.data.search.Search;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

/**
 * Кэш для поиска
 */
public class SearchCacheManager extends PreferencesCacheManager {
    private static final String CACHE_KEY = "Search";
    //Одни сутки
    private static final int LIFE_TIME = 86400; //6 * 60 * 60
    private static final String CACHE_KEY_SEARCH_POSITION_POSTFIX = "_search_position";
    private static final String CACHE_KEY_SEARCH_SIGNATURE_POSTFIX = "_search_signature";

    public SearchCacheManager() {
        super();
    }

    public void setCache(Search search) {
        try {
            getEditor()
                    .putString(getDataCacheKey(CACHE_KEY), search.toJson().toString())
                    .putLong(getExpireDateCacheKey(CACHE_KEY), getExpireDateTimestamp(LIFE_TIME))
                    .putInt(getSearchPositionCacheKey(CACHE_KEY), search.getSearchPosition())
                    .putString(getSearchSignatureCacheKey(CACHE_KEY), search.getSignature())
                    .commit();
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public void saveSearchPosition(Search search) {
        Search.log("save search position to cache " + search.getSearchPosition());
        getEditor().putInt(getSearchPositionCacheKey(CACHE_KEY), search.getSearchPosition()).commit();
    }

    public void saveSearchSignature(String searchSignature) {
        Search.log("save signature to cache " + searchSignature);
        getEditor().putString(getSearchSignatureCacheKey(CACHE_KEY), searchSignature).commit();
    }

    public Search getCache() {
        return parseCacheData(super.getCache(CACHE_KEY));
    }

    private Search parseCacheData(String cache) {
        Search search = null;

        if (cache != null) {
            try {
                search = new Search(new JSONObject(cache));
                search.setSearchPosition(getSearchPosition());
                search.updateSignature();
            } catch (Exception e) {
                Debug.error(e);
            }
        }

        return search;
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
        return super.isCacheExpired(cacheKey) ||
                //Проверяем соответсвие кэша текущему фильтру поиска
                !TextUtils.equals(getSignatureFromCache(), CacheProfile.dating.getFilterSignature());
    }

    protected String getSignatureFromCache() {
        return mPreferences.getString(getSearchSignatureCacheKey(CACHE_KEY), null);
    }
}
