package com.topface.topface.utils.cache;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.topface.Static;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.Debug;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kirussell on 14.11.13.
 * Cache manager for UserList object
 */
public class UsersListCacheManager extends PreferencesCacheManager{

    private static final String CACHE_KEY_SEARCH_POSITION_POSTFIX = "_search_position";

    private String mCacheKey;
    private Class mItemClass;

    public UsersListCacheManager(String key, Class itemClass) {
        mCacheKey = key;
        mItemClass = itemClass;
    }

    public void changeCacheKeyTo(String key, Class itemClass) {
        mCacheKey = key;
        mItemClass = itemClass;
    }

    public final void setCache(final UsersList usersList) {
        new BackgroundThread() {
            @Override
            public void execute() {
                try {
                    SharedPreferences.Editor editor = getEditor();
                    saveToCache(editor, usersList);
                    editor.commit();
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        };
    }

    /**
     * Called before commit on setCache(...) in BackgroundThread
     * You have to write values to cache here
     * Note: commit will be called after
     * TODO hide editor object from outside code
     * @param editor editor
     * @param usersList users
     * @throws JSONException
     */
    protected void saveToCache(SharedPreferences.Editor editor, UsersList usersList) throws JSONException {
        editor.putString(getDataCacheKey(mCacheKey), usersList.toJson().toString());
        editor.putInt(getPositionCacheKey(mCacheKey), usersList.getSearchPosition());
    }

    @Nullable
    public UsersList getCache() {
        return parseCacheData(super.getCache(mCacheKey));
    }

    @Nullable
    public UsersList getCacheAndRemove() {
        UsersList result = parseCacheData(super.getCache(mCacheKey));
        setCache(mCacheKey, Static.EMPTY, 0);
        return result;
    }

    @SuppressWarnings("unchecked")
    private UsersList parseCacheData(String cache) {
        UsersList usersList = null;

        if (cache != null && !TextUtils.isEmpty(cache)) {
            try {
                usersList = new UsersList(new JSONObject(cache), mItemClass);
                usersList.setSearchPosition(getPosition());
                usersList.updateSignature();
            } catch (Exception e) {
                Debug.error(e);
            }
        }

        return usersList;
    }

    public int getPosition() {
        return mPreferences.getInt(getPositionCacheKey(mCacheKey), 0);
    }

    public void clearCache() {
        getEditor()
                .remove(getPositionCacheKey(mCacheKey))
                .remove(getDataCacheKey(mCacheKey))
                //.remove(getExpireDateCacheKey(mCacheKey))
                .commit();
    }

    protected String getPositionCacheKey(String cacheKey) {
        return CACHE_KEY_PREFIX + cacheKey + CACHE_KEY_SEARCH_POSITION_POSTFIX;
    }

    @Override
    protected boolean isCacheExpired(String cacheKey) {
        return false;
    }
}
