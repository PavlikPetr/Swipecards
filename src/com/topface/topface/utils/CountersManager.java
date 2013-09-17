package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.LeadersRequest;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;

public class CountersManager {
    private static int likesCounter;
    private static int sympathyCounter;
    private static int visitorsCounter;
    private static int dialogsCounter;
    private static int fansCounter;

    private Context mContext;

    private final static String[] DeniedMethod = {BannerRequest.SERVICE_NAME, LeadersRequest.SERVICE_NAME};
    public final static String UPDATE_COUNTERS = "com.topface.topface.UPDATE_COUNTERS";

    public final static String NULL_METHOD = "null_method";
    public final static String CHANGED_BY_GCM = "gcm_changed";
    public final static String METHOD_INTENT_STRING = "method";

    public final static int UNKNOWN_TYPE = -1;
    public final static int LIKES = 0;
    public final static int SYMPATHY = 1;
    public final static int VISITORS = 2;
    public final static int DIALOGS = 3;
    public final static int FANS = 4;

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
    }

    public void setCounter(int type, int value, boolean doNeedUpdate) {
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
        }
    }

    public void setEntitiesCounters(int likes, int mutual, int dialogs, int visitors, int fans) {
        CountersManager.likesCounter = likes;
        CountersManager.sympathyCounter = mutual;
        CountersManager.dialogsCounter = dialogs;
        CountersManager.visitorsCounter = visitors;
        CountersManager.fansCounter = fans;
        commitCounters();
    }

    public void setBalanceCounters(int likes, int money) {
        CacheProfile.likes = likes;
        CacheProfile.money = money;
        updateUICounters();
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
        }
        return -1;
    }

    public CountersManager setMethod(String method) {
        lastRequestMethod = method;
        return this;
    }

    private void commitCounters() {
        if (likesCounter != CacheProfile.unread_likes ||
                dialogsCounter != CacheProfile.unread_messages ||
                sympathyCounter != CacheProfile.unread_mutual ||
                visitorsCounter != CacheProfile.unread_visitors ||
                fansCounter != CacheProfile.unread_fans) {
            if (CacheProfile.unread_likes < likesCounter) {
                LikesClosingFragment.usersProcessed = false;
            }
            CacheProfile.unread_likes = likesCounter;
            CacheProfile.unread_messages = dialogsCounter;
            CacheProfile.unread_mutual = sympathyCounter;
            CacheProfile.unread_visitors = visitorsCounter;
            CacheProfile.unread_fans = fansCounter;
            updateUICounters();
        }
    }

    /**
        кидаем broadcast о том, что счетчики обновились и причину их обновления
        название метода, если это запрос, или константу, если это GCM
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

    private boolean checkMethodIsDenyed(String method) {
        for (String denyed : DeniedMethod) {
            if (denyed.equals(method)) {
                return true;
            }
        }
        return false;
    }
}
