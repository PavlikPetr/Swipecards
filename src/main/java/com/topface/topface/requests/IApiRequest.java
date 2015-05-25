package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.transport.Headers;

public interface IApiRequest {

    IApiRequest callback(ApiHandler handler);

    public void exec();

    public int resend();

    void setEmptyHandler();

    public String getApiUrl();

    public void cancel();

    public boolean isCanResend();

    public boolean isCanceled();

    public void setFinished();

    String getRequestBodyData();

    String getContentType();

    public Context getContext();

    public ApiHandler getHandler();

    public String getId();

    String getServiceName();

    public IApiResponse sendRequestAndReadResponse() throws Exception;

    boolean isNeedAuth();

    public Headers getHeaders(String transport);

    public void sendHandlerMessage(IApiResponse response);

    RequestBuilder intoBuilder(RequestBuilder requestBuilder);

    boolean containsAuth();
}
