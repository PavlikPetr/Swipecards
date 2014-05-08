package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ApiHandler;

public interface IApiRequest {

    public void exec();

    public int resend();

    public void cancel();

    public boolean isCanResend();

    public boolean isCanceled();

    public void setFinished();

    public Context getContext();

    public ApiHandler getHandler();

    public String getId();

    public IApiResponse sendRequestAndReadResponse() throws Exception;

    IApiResponse constructApiResponse(int code, String message);

    boolean isNeedAuth();

    public void sendHandlerMessage(IApiResponse response);
}
