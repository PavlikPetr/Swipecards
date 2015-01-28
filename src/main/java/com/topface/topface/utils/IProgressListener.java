package com.topface.topface.utils;

/**
 * Created by kirussell on 15/01/15.
 * Callback for progress tracking
 */
public interface IProgressListener {

    public void onProgress(int percentage);

    public void onSuccess();
}
