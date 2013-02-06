package com.topface.topface.data.search;


import com.topface.topface.utils.cache.SearchCacheManager;

import java.util.Collection;

public class CachableSearch extends Search {

    private final SearchCacheManager mCache;

    /**
     * Сколько оцененных пользователей храним в кэше
     * NOTE: Сейчас этот параметр скорее задел на будущее, так как если число будет меньше чем в RATED_USERS_CNT,
     * то будут очищены и оцененные пользователи и в памяти
     */
    public static final int RATED_USERS_IN_CACNE_CNT = 8;

    public CachableSearch() {
        super();

        mCache = new SearchCacheManager();
        super.replace(mCache.getCache());
    }

    public void saveCache() {
        //В кеше храним ограниченное количество оцененных пользователей
        removeRatedUsers(RATED_USERS_IN_CACNE_CNT);
        mCache.setCache(this);
    }

    protected boolean removeRatedUsers(int removeCnt) {
        boolean result = super.removeRatedUsers(removeCnt);
        if (result) {
            mCache.setCache(this);
        }

        return result;
    }

    @Override
    public void setSearchPosition(int position) {
        if (getSearchPosition() != position) {
            super.setSearchPosition(position);
            mCache.saveSearchPosition(this);
        }
    }

    @Override
    public void add(int location, SearchUser object) {
        super.add(location, object);
        saveCache();
    }

    @Override
    public boolean add(SearchUser object) {
        boolean result = super.add(object);
        saveCache();
        return result;
    }

    @Override
    public boolean addAll(int location, Collection<? extends SearchUser> collection) {
        boolean result = super.addAll(location, collection);
        saveCache();
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends SearchUser> collection) {
        boolean result = super.addAll(collection);
        saveCache();
        return result;
    }

    @Override
    public void addFirst(SearchUser object) {
        super.addFirst(object);
        saveCache();
    }

    @Override
    public void addLast(SearchUser object) {
        super.addLast(object);
        saveCache();
    }

    @Override
    public void replace(Search search) {
        super.replace(search);
        if (search != null) {
            saveCache();
        } else {
            mCache.clearCache();
        }
    }

    public boolean setSignature(String signature) {
        boolean result = super.setSignature(signature);
        if (result) {
            mCache.clearCache();
            mCache.saveSearchSignature(signature);
        }

        return result;
    }

}
