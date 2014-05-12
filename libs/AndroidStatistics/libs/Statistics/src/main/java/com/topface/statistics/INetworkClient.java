package com.topface.statistics;

/**
 * Created by kirussell on 15.04.2014.
 * Any Network client that sends requests on defined url
 */
public interface INetworkClient {
    void sendRequest(String data, IRequestCallback callback);

    INetworkClient setUrl(String url);

    INetworkClient setUserAgent(String userAgent);
}
