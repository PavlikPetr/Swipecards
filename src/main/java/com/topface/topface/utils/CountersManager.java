package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.state.TopfaceAppState;

import org.json.JSONObject;

import javax.inject.Inject;

public class CountersManager {
    @Inject
    TopfaceAppState mAppState;
    public static final String UPDATE_VIP_STATUS = "com.topface.topface.UPDATE_VIP_STATUS";

    private Context mContext;

    public final static String NULL_METHOD = "null_method";
    public final static String METHOD_INTENT_STRING = "method";
    public static final String VIP_STATUS_EXTRA = "vip_status";

    public final static int UNKNOWN_TYPE = -1;
    public final static int LIKES = 0;

    private static CountersManager mInstance;

    public static CountersManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CountersManager(context);
        }
        return mInstance;
    }

    private CountersManager(Context context) {
        mContext = context;
    }

    public void setEntitiesCounters(JSONObject unread) {
        if (unread == null) {
            return;
        }
        App.from(mContext).inject(this);
        CountersData countersData = JsonUtils.fromJson(unread.toString(), CountersData.class);
        if (countersData != null) {
            mAppState.setData(countersData);
        }
    }

    public void setBalanceCounters(JSONObject balanceJson) {
        if (balanceJson == null) {
            return;
        }
        App.from(mContext).inject(this);
        BalanceData balanceData = JsonUtils.fromJson(balanceJson.toString(), BalanceData.class);
        mAppState.setData(balanceData);
        if (balanceData.premium != CacheProfile.premium) {
            App.sendProfileAndOptionsRequests();
            Intent intent = new Intent(UPDATE_VIP_STATUS);
            intent.putExtra(VIP_STATUS_EXTRA, balanceData.premium);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
}
