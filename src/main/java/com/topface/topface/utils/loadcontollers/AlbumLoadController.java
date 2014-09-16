package com.topface.topface.utils.loadcontollers;

import com.topface.topface.receivers.ConnectionChangeReceiver;

import java.util.HashMap;

import static com.topface.topface.receivers.ConnectionChangeReceiver.*;
import static com.topface.topface.receivers.ConnectionChangeReceiver.ConnectionType.*;

/**
 * Created by ilya on 12.09.14.
 */
public class AlbumLoadController extends LoadController{

    public static final int FOR_GALLERY = 0;
    public static final int FOR_PREVIEW = 1;

    private int mType;

    public AlbumLoadController(int type) {
        mType = type;
    }

    @Override
    protected void feelOffsetMap(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> offsetMap) {
        switch (mType) {
            case FOR_GALLERY:
                feelOffsetMapForGallery(offsetMap);
                break;
            case FOR_PREVIEW:
                feelOffsetMapForPreview(offsetMap);
                break;
        }
    }

    private void feelOffsetMapForGallery(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> offsetMap) {
        offsetMap.put(CONNECTION_WIFI, 0);
        offsetMap.put(CONNECTION_MOBILE_3G, 0);
        offsetMap.put(CONNECTION_MOBILE_EDGE,5);
        offsetMap.put(CONNECTION_OFFLINE, 0);
    }

    private void feelOffsetMapForPreview(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> offsetMap) {
        offsetMap.put(CONNECTION_WIFI, 0);
        offsetMap.put(CONNECTION_MOBILE_3G, 0);
        offsetMap.put(CONNECTION_MOBILE_EDGE,5);
        offsetMap.put(CONNECTION_OFFLINE, 0);
    }

    @Override
    protected void feelPreloadLimitMap(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> preloadMap) {
        switch (mType) {
            case FOR_GALLERY:
                feelPreloadLimitMapForGallery(preloadMap);
                break;
            case FOR_PREVIEW:
                feelPreloadLimitMapForPreview(preloadMap);
                break;
        }
    }

    private void feelPreloadLimitMapForGallery(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> preloadMap) {
        preloadMap.put(CONNECTION_WIFI, 40);
        preloadMap.put(CONNECTION_MOBILE_3G, 20);
        preloadMap.put(CONNECTION_MOBILE_EDGE, 10);
        preloadMap.put(CONNECTION_OFFLINE, 0);
    }

    private void feelPreloadLimitMapForPreview(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> preloadMap) {
        preloadMap.put(CONNECTION_WIFI, 40);
        preloadMap.put(CONNECTION_MOBILE_3G, 20);
        preloadMap.put(CONNECTION_MOBILE_EDGE, 10);
        preloadMap.put(CONNECTION_OFFLINE, 0);
    }
}
