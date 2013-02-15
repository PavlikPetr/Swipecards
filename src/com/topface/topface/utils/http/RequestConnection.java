package com.topface.topface.utils.http;

import com.topface.topface.requests.IApiRequest;
import com.topface.topface.utils.Debug;

import java.net.HttpURLConnection;

public class RequestConnection {
    private HttpURLConnection mConnection;
    private IApiRequest mApiRequest;

    public RequestConnection(IApiRequest apiRequest) {
        mApiRequest = apiRequest;
    }

    public void abort() {
        try {
            if (mConnection != null) {
                mConnection.disconnect();
                mConnection = null;
            }

            mApiRequest = null;

        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public void setConnection(HttpURLConnection connection) {
        mConnection = connection;
    }

    public IApiRequest getApiRequest() {
        return mApiRequest;
    }

}
