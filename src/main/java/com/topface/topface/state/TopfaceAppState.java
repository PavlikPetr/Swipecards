package com.topface.topface.state;

import com.topface.topface.data.BalanceData;

/**
 * Created by ppetr on 15.06.15.
 * child AppState class for Topface application
 */
public class TopfaceAppState extends AppState1 {
    public TopfaceAppState(CacheDataInterface listener) {
        super(listener);
    }

    public TopfaceAppState() {
        super(null);
    }

    public BalanceData getBalance() {
        return getNotNullData(new BalanceData(false, 0, 0));
    }
}
