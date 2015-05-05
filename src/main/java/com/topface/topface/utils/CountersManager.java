package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.App;
import com.topface.topface.requests.BannerRequest;

import org.json.JSONObject;

public class CountersManager {
    public static final String UPDATE_BALANCE = "com.topface.topface.UPDATE_BALANCE";
    public final static String UPDATE_COUNTERS = "com.topface.topface.UPDATE_COUNTERS";
    public static final String UPDATE_VIP_STATUS = "com.topface.topface.UPDATE_VIP_STATUS";
    private static int likesCounter;
    private static int sympathyCounter;
    private static int visitorsCounter;
    private static int dialogsCounter;
    private static int fansCounter;
    private static int admirationsCounter;
    private static int geoCounter;

    private Context mContext;

    public final static String NULL_METHOD = "null_method";
    public final static String CHANGED_BY_GCM = "gcm_changed";
    public final static String METHOD_INTENT_STRING = "method";
    public static final String VIP_STATUS_EXTRA = "vip_status";

    public final static int UNKNOWN_TYPE = -1;
    public final static int LIKES = 0;
    public final static int SYMPATHY = 1;
    public final static int VISITORS = 2;
    public final static int DIALOGS = 3;
    public final static int FANS = 4;
    public final static int ADMIRATIONS = 5;
    public final static int GEO = 6;

    private static String lastRequestMethod;

    private static CountersManager mInstance;

    public static CountersManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CountersManager(context);
        }
        return mInstance;
    }

    private CountersManager(Context context) {
        mContext = context;
        likesCounter = CacheProfile.unread_likes;
        sympathyCounter = CacheProfile.unread_mutual;
        visitorsCounter = CacheProfile.unread_visitors;
        dialogsCounter = CacheProfile.unread_messages;
        fansCounter = CacheProfile.unread_fans;
        admirationsCounter = CacheProfile.unread_admirations;
        geoCounter = CacheProfile.unread_geo;
    }

    public void setCounter(int type, int value) {
        switch (type) {
            case LIKES:
                likesCounter = value;
                commitCounters();
                break;
            case SYMPATHY:
                sympathyCounter = value;
                commitCounters();
                break;
            case VISITORS:
                visitorsCounter = value;
                commitCounters();
                break;
            case DIALOGS:
                dialogsCounter = value;
                commitCounters();
                break;
            case FANS:
                fansCounter = value;
                commitCounters();
                break;
            case ADMIRATIONS:
                admirationsCounter = value;
                commitCounters();
                break;
            case GEO:
                geoCounter = value;
                break;
        }
    }

    public void setEntitiesCounters(int likesCounter, int sympathyCounter, int dialogsCounter, int visitorsCounter, int fansCounter, int admirationsCounter, int geoCounter) {
        CountersManager.likesCounter = likesCounter;
        CountersManager.sympathyCounter = sympathyCounter;
        CountersManager.dialogsCounter = dialogsCounter;
        CountersManager.visitorsCounter = visitorsCounter;
        CountersManager.fansCounter = fansCounter;
        CountersManager.admirationsCounter = admirationsCounter;
        CountersManager.geoCounter = geoCounter;
        commitCounters();
    }

    public void setBalanceCounters(JSONObject balanceJson) {
        if (balanceJson == null) return;
        setBalanceCounters(
                balanceJson.optInt("likes"),
                balanceJson.optInt("money")
        );
        boolean premiumStatus = balanceJson.optBoolean("premium", CacheProfile.premium);
        if (premiumStatus != CacheProfile.premium) {
            CacheProfile.premium = premiumStatus;
            App.sendProfileAndOptionsRequests();
            Intent intent = new Intent(UPDATE_VIP_STATUS);
            intent.putExtra(VIP_STATUS_EXTRA, premiumStatus);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
        updateBalanceCounters();
    }

    public void setBalanceCounters(int likes, int money) {
        CacheProfile.likes = likes;
        CacheProfile.money = money;
        updateBalanceCounters();
    }

    public int getCounter(int type) {
        switch (type) {
            case LIKES:
                return likesCounter;
            case SYMPATHY:
                return sympathyCounter;
            case DIALOGS:
                return dialogsCounter;
            case VISITORS:
                return visitorsCounter;
            case FANS:
                return fansCounter;
            case ADMIRATIONS:
                return admirationsCounter;
            case GEO:
                return geoCounter;
        }
        return -1;
    }

    public CountersManager setMethod(String method) {
        lastRequestMethod = method;
        return this;
    }

    private void commitCounters() {
        String method = lastRequestMethod == null ? NULL_METHOD : lastRequestMethod;
        //надо как-нибудь отрефакторить эту жесть в будущем
        if ((likesCounter != CacheProfile.unread_likes ||
                dialogsCounter != CacheProfile.unread_messages ||
                sympathyCounter != CacheProfile.unread_mutual ||
                visitorsCounter != CacheProfile.unread_visitors ||
                fansCounter != CacheProfile.unread_fans ||
                admirationsCounter != CacheProfile.unread_admirations ||
                geoCounter != CacheProfile.unread_geo) &&
                !checkMethodIsDenyed(method)
                ) {
            CacheProfile.unread_likes = likesCounter;
            CacheProfile.unread_messages = dialogsCounter;
            CacheProfile.unread_mutual = sympathyCounter;
            CacheProfile.unread_visitors = visitorsCounter;
            CacheProfile.unread_fans = fansCounter;
            CacheProfile.unread_admirations = admirationsCounter;
            CacheProfile.unread_geo = geoCounter;
            updateUICounters();
        }
    }

    /**
     * кидаем broadcast о том, что счетчики обновились и причину их обновления
     * название метода, если это запрос, или константу, если это GCM
     */
    private void updateUICounters() {
        String method = lastRequestMethod == null ? NULL_METHOD : lastRequestMethod;
        if (!checkMethodIsDenyed(method)) {
            Intent intent = new Intent(UPDATE_COUNTERS);
            intent.putExtra(METHOD_INTENT_STRING, method);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
        setMethod(NULL_METHOD);
    }

    private void updateBalanceCounters() {
        Intent intent = new Intent(UPDATE_BALANCE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private boolean checkMethodIsDenyed(String method) {
        return BannerRequest.SERVICE_NAME.equals(method);
    }
}
