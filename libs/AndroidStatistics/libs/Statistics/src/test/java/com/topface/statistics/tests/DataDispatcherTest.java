package com.topface.statistics.tests;

import com.topface.statistics.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirussell on 19.04.2014.
 *
 */
public class DataDispatcherTest extends TestCase {

    public void testDispatch() {
        DummyNetworkClient client = new DummyNetworkClient();
        IHitDataBuilder builder = new DummyHitDataBuilder();
        IDataDispatcher dispatcher = new NetworkDataDispatcher(client);
        dispatcher.setDataBuilder(builder);
        List<String> data = new ArrayList<>();
        data.add("data" + 1);
        data.add("data" + 2);
        dispatcher.dispatchData(data);
        assertEquals(builder.build(data), client.lastDispatchedData);
    }

    private class DummyNetworkClient implements INetworkClient {

        String lastDispatchedData;

        @Override
        public void sendRequest(String data, IRequestCallback callback) {
            lastDispatchedData = data;
            callback.onSuccess();
            callback.onEnd();
        }

        @Override
        public INetworkClient setUrl(String url) {
            return this;
        }

        @Override
        public INetworkClient setUserAgent(String userAgent) {
            return this;
        }
    }

}
