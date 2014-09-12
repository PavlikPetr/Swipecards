package com.topface.topface.utils;

import android.content.BroadcastReceiver;

import com.topface.topface.App;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.utils.config.AppConfig;

import java.util.HashMap;

public class FeedLoadController {
    HashMap<ConnectionChangeReceiver.ConnectionType, Integer> feedItemsCount = new HashMap<>(); //Число элементов которые надо подгрузить
    HashMap<ConnectionChangeReceiver.ConnectionType, Integer> feedItemsOffset = new HashMap<>(); //Число элементов за которыми надо начать подгрузку новых

    private Integer mCurrentFeedCount = 0;
    private Integer mCurrentFeedOffset = 0;

    public FeedLoadController() {
        feelFeedCountMap();
        feelOffsetMap();

        initFeedParametres(ConnectionChangeReceiver.getConnectionType());

        ConnectionChangeReceiver.setOnConnectionChangedListener(new ConnectionChangeReceiver.OnConnectionChangedListener() {
            @Override
            public void onConnectionChanged(ConnectionChangeReceiver.ConnectionType type) {
                initFeedParametres(type);
            }
        });
    }

    private void initFeedParametres(ConnectionChangeReceiver.ConnectionType type) {
        AppConfig config = App.getAppConfig();
        if (config.getDebugConnectionChecked()) {
            type = ConnectionChangeReceiver.ConnectionType.valueOf(config.getDebugConnection());
        }
        mCurrentFeedCount = feedItemsCount.get(type);
        mCurrentFeedOffset = feedItemsOffset.get(type);
    }

    private void feelFeedCountMap() {
        feedItemsCount.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_WIFI, 40);
        feedItemsCount.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_3G, 20);
        feedItemsCount.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_EDGE, 10);
        feedItemsCount.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_OFFLINE, 0);
    }

    private void feelOffsetMap() {
        feedItemsOffset.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_WIFI, 0);
        feedItemsOffset.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_3G, 0);
        feedItemsOffset.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_EDGE,5);
        feedItemsOffset.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_OFFLINE, 0);
    }

    public int getFeedCountByConnectionType() {
        return mCurrentFeedCount;
    }

    public int getFeedOffsetByConnectionType() {
        return mCurrentFeedOffset;
    }
}
