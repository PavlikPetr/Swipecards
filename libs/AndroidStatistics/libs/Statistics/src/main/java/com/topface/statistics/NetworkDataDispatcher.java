package com.topface.statistics;

import java.util.List;

/**
 * Created by kirussell on 14.04.2014.
 * Network implementation for dispatcher/
 * Sends built data (constructed with passed data builder) with passed NetworkClient
 */
public class NetworkDataDispatcher implements IDataDispatcher {

    private IHitDataBuilder mDataBuilder;
    private INetworkClient mNetworkClient;
    private ILogger mLogger;

    public NetworkDataDispatcher(INetworkClient networkClient) {
        mNetworkClient = networkClient;
    }

    @Override
    public void dispatchData(final List<String> data) {
        if (data != null && mNetworkClient != null && mDataBuilder != null) {
            mNetworkClient.sendRequest(mDataBuilder.build(data), new IRequestCallback() {
                @Override
                public void onSuccess() {
                    if (mLogger != null) {
                        mLogger.log("Data dispatched successfully (" + data.size() + ")");
                    }
                }

                @Override
                public void onFail() {
                    if (mLogger != null) {
                        mLogger.log("Data dispatch failed (" + data.size() + ")");
                    }
                }

                @Override
                public void onEnd() {
                }
            });
        }
    }

    @Override
    public NetworkDataDispatcher setDataBuilder(IHitDataBuilder dataBuilder) {
        mDataBuilder = dataBuilder;
        return this;
    }

    @Override
    public IDataDispatcher setLogger(ILogger logger) {
        mLogger = logger;
        return this;
    }
}