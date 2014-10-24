package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ApiHandler;

public interface IApiRequest {

    IApiRequest callback(ApiHandler handler);

    public void exec();

    public int resend();

    void setEmptyHandler();

    public void cancel();

    public boolean isCanResend();

    public boolean isCanceled();

    public void setFinished();

    String toPostData();

    public Context getContext();

    public ApiHandler getHandler();

    int getResendCounter();

    public String getId();

    String getServiceName();

    public IApiResponse sendRequestAndReadResponse() throws Exception;

    IApiResponse constructApiResponse(int code, String message);

    boolean isNeedAuth();

    public void sendHandlerMessage(IApiResponse response);

    RequestBuilder intoBuilder(RequestBuilder requestBuilder);
}
