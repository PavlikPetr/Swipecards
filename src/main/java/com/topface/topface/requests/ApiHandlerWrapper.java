package com.topface.topface.requests;

import android.content.Context;
import android.os.Message;

import com.topface.topface.requests.handlers.ApiHandler;

/**
 * Created by ppetr on 04.08.15.
 * wrapper fo DataApiHandler overrides all ApiHandler callbacks
 */
public class ApiHandlerWrapper<T> extends DataApiHandler<T> {
    private ApiHandler mApiHandler;

    public ApiHandlerWrapper(ApiHandler dataApiHandler) {
        mApiHandler = dataApiHandler;
    }

    @Override
    protected void success(T data, IApiResponse response) {
        if (mApiHandler instanceof DataApiHandler) {
            ((DataApiHandler) mApiHandler).success(data, response);
        } else {
            mApiHandler.success(response);
        }
    }

    @Override
    protected T parseResponse(ApiResponse response) {
        if (mApiHandler instanceof DataApiHandler) {
            return ((DataApiHandler<T>) mApiHandler).parseResponse(response);
        }
        return null;
    }

    @Override
    public void fail(int codeError, IApiResponse response) {
        mApiHandler.fail(codeError, response);
    }

    @Override
    public void handleMessage(Message msg) {
        mApiHandler.handleMessage(msg);
        super.handleMessage(msg);
    }

    @Override
    public void response(IApiResponse response) {
        mApiHandler.response(response);
    }

    @Override
    public void always(IApiResponse response) {
        mApiHandler.always(response);
        super.always(response);
    }

    @Override
    public void cancel() {
        mApiHandler.cancel();
        super.cancel();
    }

    @Override
    public void setCancel(boolean value) {
        mApiHandler.setCancel(value);
        super.setCancel(value);
    }

    @Override
    public void setContext(Context context) {
        mApiHandler.setContext(context);
        super.setContext(context);
    }

    @Override
    public void setNeedCounters(boolean needCounter) {
        mApiHandler.setNeedCounters(needCounter);
        super.setNeedCounters(needCounter);
    }

    @Override
    public void setOnCompleteAction(CompleteAction completeAction) {
        mApiHandler.setOnCompleteAction(completeAction);
        super.setOnCompleteAction(completeAction);
    }
}
