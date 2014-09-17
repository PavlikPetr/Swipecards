package com.topface.topface.utils.loadcontollers;

import com.topface.topface.App;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.utils.config.AppConfig;

import java.util.HashMap;

import static com.topface.topface.receivers.ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_3G;
import static com.topface.topface.receivers.ConnectionChangeReceiver.ConnectionType.CONNECTION_MOBILE_EDGE;
import static com.topface.topface.receivers.ConnectionChangeReceiver.ConnectionType.CONNECTION_WIFI;

/**
 * Created by ilya on 12.09.14.
 */
public abstract class LoadController {

    HashMap<ConnectionChangeReceiver.ConnectionType, Integer> mPreloadItemsLimit = new HashMap<>(); //Число элементов которые надо подгрузить
    HashMap<ConnectionChangeReceiver.ConnectionType, Integer> mOffsetItemsCount = new HashMap<>(); //Число элементов за которыми надо начать подгрузку новых

    public LoadController() {
        init();
    }

    protected void init() {
        feelPreloadLimitMap();
        feelOffsetMap();
    }


    protected void feelOffsetMap() {
        int[] offset = getPreloadOffset();
        if (offset != null) {
            mOffsetItemsCount.put(CONNECTION_WIFI, offset[CONNECTION_WIFI.getInt()]);
            mOffsetItemsCount.put(CONNECTION_MOBILE_3G, offset[CONNECTION_MOBILE_3G.getInt()]);
            mOffsetItemsCount.put(CONNECTION_MOBILE_EDGE, offset[CONNECTION_MOBILE_EDGE.getInt()]);
        }
    }

    protected void feelPreloadLimitMap() {
        int[] limits = getPreloadLimits();
        if (limits != null) {
            mPreloadItemsLimit.put(CONNECTION_WIFI, limits[CONNECTION_WIFI.getInt()]);
            mPreloadItemsLimit.put(CONNECTION_MOBILE_3G, limits[CONNECTION_MOBILE_3G.getInt()]);
            mPreloadItemsLimit.put(CONNECTION_MOBILE_EDGE, limits[CONNECTION_MOBILE_EDGE.getInt()]);
        }
    }

    protected abstract int[] getPreloadLimits();
    protected abstract int[] getPreloadOffset();

    public int getItemsLimitByConnectionType() {
        return mPreloadItemsLimit.get(getConnectionType());
    }

    public int getItemsOffsetByConnectionType() {
        return mOffsetItemsCount.get(getConnectionType());
    }

    private ConnectionChangeReceiver.ConnectionType getConnectionType() {
        AppConfig config = App.getAppConfig();
        if (config.getDebugConnectionChecked()) {
            return ConnectionChangeReceiver.ConnectionType.valueOf(config.getDebugConnection());
        }
        return ConnectionChangeReceiver.getConnectionType();
    }


}
