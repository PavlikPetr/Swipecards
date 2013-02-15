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

    public String toPostData();

    public Context getContext();

    public ApiHandler getHandler();

    void setSsid(String ssid);

    public String getId();
}
