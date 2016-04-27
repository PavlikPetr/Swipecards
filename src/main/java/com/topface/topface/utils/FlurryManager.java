package com.topface.topface.utils;

import android.support.annotation.StringDef;
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

    @StringDef({PHONE_BOOK_INVITES, SMS_INVITES, VK_INVITES})
    public @interface InvitesType {
    }

    public static final String PHONE_BOOK_INVITES = "phone_book";
    public static final String SMS_INVITES = "send_sms";
    public static final String VK_INVITES = "vk";

    @StringDef({SHOW, PRODUCT_BOUGHT, CLICK_BUY, CLICK_DELETE})
    public @interface PayWallAction {
    }

    public static final String SHOW = "show";
    public static final String PRODUCT_BOUGHT = "product_bought";
    public static final String CLICK_BUY = "click_buy";
    public static final String CLICK_DELETE = "click_delete";

    @StringDef({PEOPLE_NEARBY_UNLOCK, LIKES_UNLOCK, BUY_GIFT, GET_LEAD, SEND_ADMIRATION})
    public @interface ByCoinsProductType {
    }

    public static final String PEOPLE_NEARBY_UNLOCK = "people_nearby_unlock";
    public static final String LIKES_UNLOCK = "likes_unlock";
    public static final String BUY_GIFT = "buy_gift";
    public static final String GET_LEAD = "get_lead";
    public static final String SEND_ADMIRATION = "send_admiration";

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
    private static final String EMPTY_USER_ID_HASH = Utils.EMPTY;

    private String mUserIdHash = EMPTY_USER_ID_HASH;
    private static FlurryManager mInstance;

    public synchronized static FlurryManager getInstance() {
        if (mInstance == null) {
            mInstance = new FlurryManager();
        }
        return mInstance;
    }

    private String getUserIdHash() {
        int uid = CacheProfile.uid;
        if (AuthToken.getInstance().isEmpty() || uid == 0) {
            dropUserIdHash();
            return EMPTY_USER_ID_HASH;
        } else {
            if (mUserIdHash.equals(EMPTY_USER_ID_HASH)) {
                mUserIdHash = new EncryptMethods().encryptUid(uid, EMPTY_USER_ID_HASH);
            }
            return mUserIdHash;
        }
    }

    public void dropUserIdHash() {
        mUserIdHash = EMPTY_USER_ID_HASH;
    }

    public void init() {
        FlurryAgent.setLogEnabled(Debug.isDebugLogsEnabled());
        FlurryAgent.setLogLevel(Log.VERBOSE);
        FlurryAgent.setFlurryAgentListener(new FlurryAgentListener() {
            @Override
            public void onSessionStarted() {
                sendAppStartEvent();
            }
        });
        FlurryAgent.init(App.getContext(), App.getContext().getResources().getString(R.string.flurry_key));
    }

    /**
     * Send event about user authorize
     *
     * @param socialName set social net type - fb/vk/st/ok
     */
    public void sendAuthEvent(String socialName) {
        Map<String, String> socialType = new HashMap<>();
        socialType.put(SOCIAL_TYPE_PARAM, socialName);
        sendEvent(AUTH_EVENT, socialType);
    }

    /**
     * Send logout event
     */
    public void sendLogoutEvent() {
        sendEvent(LOGOUT_EVENT);
    }

    /**
     * Send open external url event
     *
     * @param url url which we try to show
     */
    public void sendExternalUrlEvent(String url) {
        Map<String, String> externalUrl = new HashMap<>();
        externalUrl.put(EXTERNAL_URL_PARAM, url);
        sendEvent(EXTERNAL_URL_EVENT, externalUrl);
    }

    /**
     * Send App start
     */
    public void sendAppStartEvent() {
        sendEvent(APP_START_EVENT);
    }

    /**
     * Send App go to background mode
     */
    public void sendAppInBackgroundEvent() {
        sendEvent(APP_BACKGROUND_EVENT);
    }

    /**
     * Send App go to foreground mode
     */
    public void sendAppInForegroundEvent() {
        sendEvent(APP_FOREGROUND_EVENT);
    }

    /**
     * Send invite friends event
     *
     * @param count set count of friend, which can get invite
     */
    public void sendInviteEvent(@InvitesType String type, int count) {
        Map<String, String> invites = new HashMap<>();
        invites.put(INVITES_COUNT_PARAM, String.valueOf(count));
        invites.put(INVITES_TYPE_PARAM, type);
        sendEvent(INVITE_EVENT, invites);
    }

    /**
     * Send event - Dating list is empty
     */
    public void sendEmptyDatingListEvent() {
        sendEvent(EMPTY_SEARCH_EVENT);
    }

    /**
     * Send event - Filter changed
     */
    public void sendFilterChangedEvent() {
        sendEvent(FILTER_CHANGED_EVENT);
    }

    /**
     * Send event - Page open
     */
    public void sendPageOpenEvent(String name) {
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
    public void sendPurchaseEvent(String id, double price, String currency) {
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
    public void sendPayWallEvent(String popupName, @PayWallAction String action) {
        Map<String, String> payWall = new HashMap<>();
        payWall.put(PAY_WALL_NAME_PARAM, popupName);
        payWall.put(PAY_WALL_ACTION_PARAM, action);
        sendEvent(PAY_WALL_EVENT, payWall);
    }

    /**
     * Send spend coins event
     *
     * @param coinsCount product price in coins
     * @param product    type of product
     */
    public void sendSpendCoinsEvent(int coinsCount, @ByCoinsProductType String product) {
        Map<String, String> payWall = new HashMap<>();
        payWall.put(PRODUCT_TYPE_PARAM, product);
        payWall.put(PRICE_PARAM, String.valueOf(coinsCount));
        sendEvent(SPEND_COINS_EVENT, payWall);
    }

    /**
     * Send event - get new full dialog (2 input message and 2 output)
     */
    public void sendFullDialogEvent() {
        sendEvent(FULL_DIALOG_EVENT);
    }

    /**
     * Send event - user come from referrer link
     */
    public void sendReferrerEvent(AdjustAttributeData attribution) {
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

    private Map<String, String> put(Map<String, String> ref, String key, String value) {
        if (!TextUtils.isEmpty(value) && ref != null) {
            ref.put(key, value);
        }
        return ref;
    }

    private boolean sendEvent(String eventName) {
        return sendEvent(eventName, null);
    }

    private boolean sendEvent(String eventName, Map<String, String> eventParams) {
        if (FlurryAgent.isSessionActive()) {
            String encrypted = getUserIdHash();
            if (eventParams == null) {
                eventParams = new HashMap<>();
            }
            if (!encrypted.equals(EMPTY_USER_ID_HASH)) {
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
