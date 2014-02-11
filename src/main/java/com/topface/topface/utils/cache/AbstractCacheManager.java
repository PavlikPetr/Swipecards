package com.topface.topface.utils.cache;

/**
 * Абстрактный класс-хелпер для кеширования данных
 */
public abstract class AbstractCacheManager {
    public static final String CACHE_KEY_PREFIX = "CacheManager_";
    public static final String CACHE_KEY_DATA_POSTFIX = "_data";
    public static final String CACHE_KEY_EXPIRE_DATE_POSTFIX = "_expire_date";

    abstract protected void setCache(String cacheKey, String data, int lifeTimeInSeconds);

    protected long getExpireDateTimestamp(int lifeTimeInSeconds) {
        return System.currentTimeMillis() + (lifeTimeInSeconds * 1000);
    }

    protected String getDataCacheKey(String cacheKey) {
        return CACHE_KEY_PREFIX + cacheKey + CACHE_KEY_DATA_POSTFIX;
    }

    protected String getExpireDateCacheKey(String cacheKey) {
        return CACHE_KEY_PREFIX + cacheKey + CACHE_KEY_EXPIRE_DATE_POSTFIX;
    }

    /**
     * Возвращает кэш в виде строки, если он есть и не просрочен
     *
     * @param cacheKey ключ кэша
     * @return строка из кэша или null
     */
    public String getCache(String cacheKey) {
        String cache = null;
        //Проверяем что кэш не просрочен
        if (!isCacheExpired(cacheKey)) {
            //Получаем данные из кэша
            cache = getDataFromCache(cacheKey);
        }

        return cache;
    }

    protected boolean isCacheExpired(String cacheKey) {
        //Получаем дату истечения жизни кэша и сравниваем с текущим системным временем
        return System.currentTimeMillis() > getExpireDateFromCache(cacheKey);
    }

    abstract protected String getDataFromCache(String cacheKey);

    abstract protected long getExpireDateFromCache(String cacheKey);
}
