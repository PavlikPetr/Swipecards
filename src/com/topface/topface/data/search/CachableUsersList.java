package com.topface.topface.data.search;


import com.topface.topface.data.FeedUser;
import com.topface.topface.utils.cache.SearchCacheManager;

public class CachableUsersList<T extends FeedUser> extends UsersList<T> {

    private final SearchCacheManager mCache;

    /**
     * Сколько оцененных пользователей храним в кэше
     * NOTE: Сейчас этот параметр скорее задел на будущее, так как если число будет меньше чем в RATED_USERS_CNT,
     * то будут очищены и оцененные пользователи и в памяти
     */
    public static final int RATED_USERS_IN_CACNE_CNT = 8;

    public CachableUsersList(Class<T> itemClass) {
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

    public boolean setSignature(final String signature) {
        boolean result = super.setSignature(signature);
        if (result) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mCache.clearCache();
                    mCache.saveSearchSignature(signature);
                }
            }).start();
        }

        return result;
    }

}
