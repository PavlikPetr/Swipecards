package com.topface.topface.utils.loadcontollers;

import com.topface.topface.App;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.utils.config.AppConfig;

import java.util.HashMap;

/**
 * Created by ilya on 12.09.14.
 */
public abstract class LoadController {

    HashMap<ConnectionChangeReceiver.ConnectionType, Integer> mPreloadItemsLimit = new HashMap<>(); //Число элементов которые надо подгрузить
    HashMap<ConnectionChangeReceiver.ConnectionType, Integer> mOffsetItemsCount = new HashMap<>(); //Число элементов за которыми надо начать подгрузку новых

    public LoadController() {
        feelPreloadLimitMap(mPreloadItemsLimit);
        feelOffsetMap(mOffsetItemsCount);
    }


    protected abstract void feelOffsetMap(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> offsetMap);

    /**
     *
     * @param preloadMap
     */
    protected abstract void feelPreloadLimitMap(HashMap<ConnectionChangeReceiver.ConnectionType, Integer> preloadMap);

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
