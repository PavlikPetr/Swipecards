package com.topface.topface.requests;

import android.os.Message;

/**
 * Handler с методом для парсинга ответа, который выполняется вне UI треда
 */
abstract public class DataApiHandler<T> extends ApiHandler {
    private T mData;

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        if (msg.obj != null) {
            mData = parseResponse((ApiResponse) msg.obj);
        }
        return super.sendMessageAtTime(msg, uptimeMillis);
    }

    @Override
    public void success(ApiResponse response) {
        success(mData, response);
    }

    protected abstract void success(T data, ApiResponse response);

    protected abstract T parseResponse(ApiResponse response);

}
