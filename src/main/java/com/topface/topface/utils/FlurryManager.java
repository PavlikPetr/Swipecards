package com.topface.topface.utils;

import android.text.TextUtils;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;
import com.topface.topface.utils.social.AuthToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ppavlik on 31.03.16.
 * hold all flurry events and method initialization sdk
 */
public class FlurryManager {

    private static final String AUTH_EVENT = "authorize";
    private static final String LOGOUT_EVENT = "logout";
    private static final String EXTERNAL_URL_EVENT = "show_external_url";
    private static final String APP_START_EVENT = "app_start";
    private static final String APP_BACKGROUND_EVENT = "app_background";
    private static final String APP_FOREGROUND_EVENT = "app_foreground";
    private static final String INVITE_EVENT = "invite";
    private static final String EMPTY_SEARCH_EVENT = "empty_search";
    private static final String FILTER_CHANGED_EVENT = "filter_changed";
    private static final String PURCHASE_EVENT = "purchase";
    private static final String PAY_WALL_EVENT = "pay_wall";
    private static final String SPEND_COINS_EVENT = "spend_coins";
    private static final String FULL_DIALOG_EVENT = "full_dialog";
    private static final String REFERRER_INSTALL_EVENT = "referrer_install";

    private static final String USER_ID_PARAM = "user_id";
    private static final String INVITES_TYPE_PARAM = "invites_type";
    private static final String SOCIAL_TYPE_PARAM = "social_type";
    private static final String EXTERNAL_URL_PARAM = "external_url";
    private static final String INVITES_COUNT_PARAM = "invites_count";
    private static final String PRODUCT_ID_PARAM = "product_id";
    private static final String PRODUCT_PRICE_PARAM = "product_price";
    private static final String PRODUCT_CURRENCY_PARAM = "product_currency";
    private static final String PAY_WALL_NAME_PARAM = "pay_wall_name";
    private static final String PAY_WALL_ACTION_PARAM = "pay_wall_action";
    private static final String PRODUCT_TYPE_PARAM = "product";
    private static final String PRICE_PARAM = "price";
    private static final String TRACKER_TOKEN_REFERRER_PARAM = "tracker_token";
    private static final String NETWORK_REFERRER_PARAM = "network";
    private static final String CAMPAIGN_REFERRER_PARAM = "campaign";
    private static final String ADGROUP_REFERRER_PARAM = "adgroup";
    private static final String CREATIVE_REFERRER_PARAM = "creative";
    private static final String CLICK_LABEL_REFERRER_PARAM = "click_label";
    private static final String TRACKER_NAME_REFERRER_PARAM = "tracker_name";

    private static final String PAGE_NAME_TEMPLATE = "page.%s";

    private static final int EMPTY_USER_ID_HASH = 0;

    private static int mUserIdHash = EMPTY_USER_ID_HASH;

    public static int getUserIdHash() {
        int uid = CacheProfile.uid;
        if (AuthToken.getInstance().isEmpty() || uid == 0) {
            dropUserIdHash();
            return EMPTY_USER_ID_HASH;
        } else {
            if (mUserIdHash == EMPTY_USER_ID_HASH) {
                mUserIdHash = String.valueOf(CacheProfile.uid).hashCode();
            }
            return mUserIdHash;
        }
    }

    public static void dropUserIdHash() {
        mUserIdHash = EMPTY_USER_ID_HASH;
    }

    public enum InvitesType {
        PHONE_BOOK_INVITES("phone_book"), SMS_INVITES("send_sms"), VK_INVITES("vk");

        private String mType;

        InvitesType(String type) {
            mType = type;
        }

        public String getType() {
            return mType;
        }
    }

    public enum PayWallAction {
        SHOW("show"), PRODUCT_BOUGHT("product_bought"), CLICK_BUY("click_buy"), CLICK_DELETE("click_delete");

        private String mAction;

        PayWallAction(String action) {
            mAction = action;
        }

        public String getAction() {
            return mAction;
        }
    }

    public enum ByCoinsProductType {
        PEOPLE_NEARBY_UNLOCK("people_nearby_unlock"), LIKES_UNLOCK("likes_unlock"), BUY_GIFT("buy_gift"),
        GET_LEAD("get_lead"), SEND_ADMIRATION("send_admiration");

        private String mProductType;

        ByCoinsProductType(String productType) {
            mProductType = productType;
        }

        public String getProductType() {
            return mProductType;
        }
    }

    public static void init() {
        FlurryAgent.init(App.getContext(), App.getContext().getResources().getString(R.string.flurry_key));
        FlurryAgent.setLogEnabled(Debug.isDebugLogsEnabled());
        FlurryAgent.setLogLevel(Log.VERBOSE);
        FlurryAgent.setFlurryAgentListener(new FlurryAgentListener() {
            @Override
            public void onSessionStarted() {
                sendAppStartEvent();
            }
        });
    }

    /**
     * Send event about user authorize
     *
     * @param socialName set social net type - fb/vk/st/ok
     */
    public static void sendAuthEvent(String socialName) {
        Map<String, String> socialType = new HashMap<>();
        socialType.put(SOCIAL_TYPE_PARAM, socialName);
        sendEvent(AUTH_EVENT, socialType);
    }

    /**
     * Send logout event
     */
    public static void sendLogoutEvent() {
        sendEvent(LOGOUT_EVENT);
    }

    /**
     * Send open external url event
     *
     * @param url url which we try to show
     */
    public static void sendExternalUrlEvent(String url) {
        Map<String, String> externalUrl = new HashMap<>();
        externalUrl.put(EXTERNAL_URL_PARAM, url);
        sendEvent(EXTERNAL_URL_EVENT, externalUrl);
    }

    /**
     * Send App start
     */
    public static void sendAppStartEvent() {
        sendEvent(APP_START_EVENT);
    }

    /**
     * Send App go to background mode
     */
    public static void sendAppInBackgroundEvent() {
        sendEvent(APP_BACKGROUND_EVENT);
    }

    /**
     * Send App go to foreground mode
     */
    public static void sendAppInForegroundEvent() {
        sendEvent(APP_FOREGROUND_EVENT);
    }

    /**
     * Send invite friends event
     *
     * @param count set count of friend, which can get invite
     */
    public static void sendInviteEvent(InvitesType type, int count) {
        Map<String, String> invites = new HashMap<>();
        invites.put(INVITES_COUNT_PARAM, String.valueOf(count));
        invites.put(INVITES_TYPE_PARAM, type.getType());
        sendEvent(INVITE_EVENT, invites);
    }

    /**
     * Send event - Dating list is empty
     */
    public static void sendEmptyDatingListEvent() {
        sendEvent(EMPTY_SEARCH_EVENT);
    }

    /**
     * Send event - Filter changed
     */
    public static void sendFilterChangedEvent() {
        sendEvent(FILTER_CHANGED_EVENT);
    }

    /**
     * Send event - Page open
     */
    public static void sendPageOpenEvent(String name) {
        if (!TextUtils.isEmpty(name)) {
            sendEvent(String.format(PAGE_NAME_TEMPLATE, name.toLowerCase()));
        }
    }

    /**
     * Send purchase event
     *
     * @param id       sku id of product
     * @param price    price value
     * @param currency currency value ISO-4217
     */
    public static void sendPurchaseEvent(String id, double price, String currency) {
        Map<String, String> purchase = new HashMap<>();
        purchase.put(PRODUCT_ID_PARAM, id);
        purchase.put(PRODUCT_PRICE_PARAM, String.valueOf(price));
        purchase.put(PRODUCT_CURRENCY_PARAM, currency);
        sendEvent(PURCHASE_EVENT, purchase);
    }

    /**
     * Send paywall event
     *
     * @param popupName dialog name
     * @param action    type of action
     */
    public static void sendPayWallEvent(String popupName, PayWallAction action) {
        Map<String, String> payWall = new HashMap<>();
        payWall.put(PAY_WALL_NAME_PARAM, popupName);
        payWall.put(PAY_WALL_ACTION_PARAM, action.getAction());
        sendEvent(PAY_WALL_EVENT, payWall);
    }

    /**
     * Send spend coins event
     *
     * @param coinsCount product price in coins
     * @param product    type of product
     */
    public static void sendSpendCoinsEvent(int coinsCount, ByCoinsProductType product) {
        Map<String, String> payWall = new HashMap<>();
        payWall.put(PRODUCT_TYPE_PARAM, product.getProductType());
        payWall.put(PRICE_PARAM, String.valueOf(coinsCount));
        sendEvent(SPEND_COINS_EVENT, payWall);
    }

    /**
     * Send event - get new full dialog (2 input message and 2 output)
     */
    public static void sendFullDialogEvent() {
        sendEvent(FULL_DIALOG_EVENT);
    }

    /**
     * Send event - user come from referrer link
     */
    public static void sendReferrerEvent(AdjustAttributeData attribution) {
        if (attribution != null) {
            Map<String, String> ref = new HashMap<>();
            ref = put(ref, ADGROUP_REFERRER_PARAM, attribution.adgroup);
            ref = put(ref, CAMPAIGN_REFERRER_PARAM, attribution.campaign);
            ref = put(ref, CLICK_LABEL_REFERRER_PARAM, attribution.clickLabel);
            ref = put(ref, CREATIVE_REFERRER_PARAM, attribution.creative);
            ref = put(ref, NETWORK_REFERRER_PARAM, attribution.network);
            ref = put(ref, TRACKER_NAME_REFERRER_PARAM, attribution.trackerName);
            ref = put(ref, TRACKER_TOKEN_REFERRER_PARAM, attribution.trackerToken);
            sendEvent(REFERRER_INSTALL_EVENT, ref);
        }
    }

    private static Map<String, String> put(Map<String, String> ref, String key, String value) {
        if (!TextUtils.isEmpty(value) && ref != null) {
            ref.put(key, value);
        }
        return ref;
    }

    private static boolean sendEvent(String eventName) {
        return sendEvent(eventName, null);
    }

    private static boolean sendEvent(String eventName, Map<String, String> eventParams) {
        if (FlurryAgent.isSessionActive()) {
            int hash = getUserIdHash();
            if (eventParams == null) {
                eventParams = new HashMap<>();
            }
            if (hash != EMPTY_USER_ID_HASH) {
                eventParams.put(USER_ID_PARAM, String.valueOf(getUserIdHash()));
            }
            if (eventParams.isEmpty()) {
                FlurryAgent.logEvent(eventName);
            } else {
                FlurryAgent.logEvent(eventName, eventParams);
            }
            return true;
        }
        return false;
    }
}
