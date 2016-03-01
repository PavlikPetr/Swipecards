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

import rx.functions.Action1;

public class CountersManager {
    public static final String CHANGED_BY_GCM = "";
    @Inject
    TopfaceAppState mAppState;
    public static final String UPDATE_VIP_STATUS = "com.topface.topface.UPDATE_VIP_STATUS";

    private Context mContext;

    public final static String NULL_METHOD = "null_method";
    public static final String VIP_STATUS_EXTRA = "vip_status";

    public final static int UNKNOWN_TYPE = -1;
    public final static int LIKES = 0;
    public final static int SYMPATHY = 1;
    public final static int VISITORS = 2;
    public final static int DIALOGS = 3;
    public final static int PEOPLE_NEARLY = 6;
    private static CountersManager mInstance;
    private String mLastRequestMeethod;
    private BalanceData mCachedBalanceData;

    public static CountersManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CountersManager(context);
        }
        return mInstance;
    }

    private CountersManager(Context context) {
        mContext = context;
        App.from(context).inject(this);
        mAppState.getObservable(BalanceData.class).subscribe(new Action1<BalanceData>() {
            @Override
            public void call(BalanceData balanceData) {
                mCachedBalanceData = balanceData;
            }
        });
    }

    public void setLastRequestMethod(String lastRequestMeethod) {
        mLastRequestMeethod = lastRequestMeethod;
    }

    public String getLastRequestMethod() {
        return mLastRequestMeethod;
    }

    public void setEntitiesCounters(JSONObject unread) {
        if (unread == null) {
            return;
        }
        CountersData countersData = JsonUtils.fromJson(unread.toString(), CountersData.class);
        if (countersData != null && !Utils.isEmptyJson(unread) && !mAppState.isEqualData(CountersData.class, countersData)) {
            mAppState.setData(countersData);
        }
    }

    public void setBalanceCounters(JSONObject balanceJson) {
        if (balanceJson == null) {
            return;
        }
        BalanceData balanceData = JsonUtils.fromJson(balanceJson.toString(), BalanceData.class);
        if (mCachedBalanceData != null && balanceData.premium != mCachedBalanceData.premium) {
            App.sendProfileAndOptionsRequests();
            Intent intent = new Intent(UPDATE_VIP_STATUS);
            intent.putExtra(VIP_STATUS_EXTRA, balanceData.premium);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
        mAppState.setData(balanceData);
    }
}
