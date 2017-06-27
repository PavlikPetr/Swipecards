package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.utils.Utils;

/**
 * Promo popup statistics
 */
public class PromoDialogStatistics {

    public static final String PROMO_DIALOG_SHOW = "promo_dialog_show";
    public static final String PROMO_DIALOG_CLOSE_AFTER_BUY_VIP = "promo_dialog_close_after_buy_vip";
    public static final String PROMO_DIALOG_CLOSE_AFTER_UPDATE_PROFILE = "promo_dialog_close_after_update_profile";
    public static final String PROMO_DIALOG_CLICK_BUY_VIP = "promo_dialog_click_buy_vip";
    public static final String PROMO_DIALOG_DISMISS = "promo_dialog_dismiss";
    public static final String POPUP_TAG = "plc";
    public static final String POPUP_TYPE = "type";
    public static final String DEFAULT_POPUP_ID = "default";

    public static Slices getSlices(String tag, String type) {
        return new Slices().putSlice(POPUP_TAG, tag).putSlice(POPUP_TYPE, type);
    }

    public static void promoDialogShowSend(String tag, String type) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_SHOW, 1, getSlices(tag, type));
    }

    public static void promoDialogCloseAfterBuyVipSend(String tag, String type) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_CLOSE_AFTER_BUY_VIP, 1, getSlices(tag, type));
    }

    public static void promoDialogCloseAfterUpdateProfileSend(String tag, String type) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_CLOSE_AFTER_UPDATE_PROFILE, 1, getSlices(tag, type));
    }

    public static void promoDialogClickBuyVipSend(String tag, String type) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_CLICK_BUY_VIP, 1, getSlices(tag, type));
    }

    public static void promoDialogDismissSend(String tag, String type) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_DISMISS, 1, getSlices(tag, type));
    }

    public static String getPopupId(String id) {
        return (id == null || id.equals(Utils.EMPTY)) ? DEFAULT_POPUP_ID : id;
    }
}
