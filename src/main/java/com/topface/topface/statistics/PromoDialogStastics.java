package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;

/**
 * Promo popup statistics
 */
public class PromoDialogStastics {

    public static final String PROMO_DIALOG_SHOW = "promo_dialog_show";
    public static final String PROMO_DIALOG_CLOSE_AFTER_BUY_VIP = "promo_dialog_close_after_buy_vip";
    public static final String PROMO_DIALOG_CLOSE_AFTER_UPDATE_PROFILE = "promo_dialog_close_after_update_profile";
    public static final String PROMO_DIALOG_CLICK_BUY_VIP = "promo_dialog_click_buy_vip";
    public static final String PROMO_DIALOG_DISMISS = "promo_dialog_dismiss";
    public static final String POPUP_TAG = "plc";

    public static Slices getSlices(String tag) {
        return new Slices().putSlice(POPUP_TAG, tag);
    }

    public static void promoDialogShowSend(String tag) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_SHOW, 1, getSlices(tag));
    }

    public static void promoDialogCloseAfterBuyVipSend(String tag) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_CLOSE_AFTER_BUY_VIP, 1, getSlices(tag));
    }

    public static void promoDialogCloseAfterUpdateProfileSend(String tag) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_CLOSE_AFTER_UPDATE_PROFILE, 1, getSlices(tag));
    }

    public static void promoDialogClickBuyVipSend(String tag) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_CLICK_BUY_VIP, 1, getSlices(tag));
    }

    public static void promoDialogDismissSend(String tag) {
        StatisticsTracker.getInstance().sendEvent(PROMO_DIALOG_DISMISS, 1, getSlices(tag));
    }
}
