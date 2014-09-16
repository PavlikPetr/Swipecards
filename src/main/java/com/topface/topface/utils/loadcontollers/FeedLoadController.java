package com.topface.topface.utils.loadcontollers;

import com.topface.topface.receivers.ConnectionChangeReceiver;

import java.util.HashMap;

public class FeedLoadController extends LoadController{

    @Override
    protected void feelOffsetMap(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> offsetMap) {
        offsetMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_WIFI, 10);
        offsetMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_3G, 5);
        offsetMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_EDGE,0);
        offsetMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_OFFLINE, 0);
    }

    @Override
    protected void feelPreloadLimitMap(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> preloadMap) {
        preloadMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_WIFI, 40);
        preloadMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_3G, 20);
        preloadMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_EDGE, 10);
        preloadMap.put(ConnectionChangeReceiver.ConnectionType.CONNECTION_OFFLINE, 0);
    }
}
