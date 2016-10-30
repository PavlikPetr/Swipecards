package com.topface.topface.data.search;


import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.data.FeedUser;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.config.UserConfig;

public class CachableSearchList<T extends FeedUser> extends UsersList<T> {

    private final SearchCacheManager mCache;

    /**
     * Сколько оцененных пользователей храним в кэше
     * NOTE: Сейчас этот параметр скорее задел на будущее, так как если число будет меньше чем в RATED_USERS_CNT,
     * то будут очищены и оцененные пользователи и в памяти
     */
    public static final int RATED_USERS_IN_CACNE_CNT = 8;

    public CachableSearchList(Class<T> itemClass) {
        super(itemClass);

        mCache = new SearchCacheManager();
        super.replace(mCache.getCache());
    }

    public void saveCache() {
        //В кеше храним ограниченное количество оцененных пользователей
        removeRatedUsers(RATED_USERS_IN_CACNE_CNT);
        log("Save in cache " + size() + " users");
        mCache.setCache(this);
    }

    /**
     * Сохранить в кэш текущего пользователя
     */
    public void saveCurrentInCache() {
        removeAllUsers();
        log("Save in Cache current user");
        mCache.setCache(this);
    }

    public boolean setSignature(final String signature) {
        boolean result = super.setSignature(signature);
        if (result) {
            new BackgroundThread() {
                @Override
                public void execute() {
                    mCache.clearCache();
                    mCache.saveSearchSignature(signature);
                }
            };
        }

        return result;
    }

    private boolean isValidUserCache() {
        UserConfig config = App.getUserConfig();
        if (config.isUserCityChanged()) {
            config.setUserCityChanged(false);
            config.saveConfig();
            return false;
        }
        return true;
    }

    private boolean isLocaleChanged() {
        UserConfig config = App.getUserConfig();
        if (config.isLocaleChanged()) {
            config.setLocaleChange(false);
            return true;
        } else {
            return false;
        }
    }

    public void clearIfNeed() {
        if (!isValidUserCache() || isLocaleChanged()) {
            clear();
        }
    }
}
