package com.topface.topface.utils.http;

import com.topface.topface.utils.Debug;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

public class RequestConnection {
    HttpPost mHttpPost;
    HttpClient mHttpClient;

    public RequestConnection() {
    }

    public RequestConnection(HttpPost httpPost, HttpClient httpClient) {
        mHttpPost = httpPost;
        mHttpClient = httpClient;
    }

    public HttpPost getHttpPost() {
        return mHttpPost;
    }

    public void setHttpPost(HttpPost httpPost) {
        this.mHttpPost = httpPost;
    }

    public HttpClient getHttpClient() {
        return mHttpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.mHttpClient = httpClient;
    }

    public void abort() {
        try {
            if (mHttpPost != null) {
                mHttpPost.abort();
            }
            if (mHttpClient != null) {
                mHttpClient.getConnectionManager().closeExpiredConnections();
            }
        } catch (Exception localException) {
            Debug.log(this, localException.toString());
        }
    }
}
