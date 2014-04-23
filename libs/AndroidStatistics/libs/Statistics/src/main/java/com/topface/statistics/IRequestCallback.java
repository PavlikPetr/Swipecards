package com.topface.statistics;

/**
 * Created by kirussell on 15.04.2014.
 * Some common interface for requests' callback
 */
public interface IRequestCallback {
    void onSuccess();
    void onFail();
    void onEnd();
}
