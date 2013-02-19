package com.topface.topface.requests;

import android.os.Message;
import com.topface.topface.requests.handlers.ApiHandler;

/**
 * Handler с методом для парсинга ответа, который выполняется вне UI треда
 */
abstract public class DataApiHandler<T> extends ApiHandler {
    private T mData;

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        if (msg.obj != null) {
            ApiResponse response = (ApiResponse) msg.obj;
            //Парсим запрос только если запрос завершился удачно
            if (response.isCompleted()) {
                mData = parseResponse(response);
            }
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
