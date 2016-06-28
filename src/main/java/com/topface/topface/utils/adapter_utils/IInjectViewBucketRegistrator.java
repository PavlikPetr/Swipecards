package com.topface.topface.utils.adapter_utils;

/**
 * Интерфейс для регистрации вставляемых вьюх.
 * Created by tiberal on 23.06.16.
 */
public interface IInjectViewBucketRegistrator {

    void registerViewBucket(InjectViewBucket bucket);

    void removeViewBucket(InjectViewBucket bucket);

    void removeAllBuckets();
}
