package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.Static;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.Http;
import com.topface.topface.utils.http.RequestConnection;

public abstract class ApiRequest {
    // Data
    public String ssid;
    public ApiHandler handler;
    public Context context;
    public boolean canceled = false;
    private RequestConnection connection;

    public ApiRequest(Context context) {
        ssid = Static.EMPTY;
        this.context = context;
    }

    public ApiRequest callback(ApiHandler handler) {
        this.handler = handler;
        return this;
    }

    public void exec() {
        connection = ConnectionManager.getInstance().sendRequest(this);
        //ConnectionManager.getInstance().sendRequestNew(this);
        //ConnectionService.sendRequest(mContext,this);
    }

    public void cancel() {
        handler = null;
        if (connection != null)
            connection.abort();
        canceled = true;
    }
}