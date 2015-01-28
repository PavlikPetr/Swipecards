package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.controllers.AbstractStartAction;
import com.topface.topface.utils.controllers.StartActionsController;

public class NotificationsDisablePopup extends AbstractStartAction {
    private Activity mActivity;

    public NotificationsDisablePopup(Activity activity) {
        mActivity = activity;
    }

    public static final String NOTIFICATION_DISABLE_POPUP_PREF_KEY = "NOTIFICATION_DISABLE_POPUP_PREF_KEY";

    private MarketApiManager mMarketApiManager;

    private void showPopup() {
        getPopup().show();
    }

    private AlertDialog getPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.google_service_general_title).setMessage(getMarketApiManager().getMessage())
                .setCancelable(true)
                .setNegativeButton(mActivity.getResources().getString(R.string.general_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mActivity = null;
                            }
                        });
        if (getMarketApiManager().isButtonVisible()) {
            builder.setPositiveButton(getMarketApiManager().getButtonText(), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    getMarketApiManager().onButtonClick();
                    dialog.cancel();
                    mActivity = null;
                }
            });
        }
        return builder.create();
    }

    @Override
    public void callInBackground() {
        SharedPreferences preferences = App.getContext().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED,
                Context.MODE_PRIVATE
        );
        preferences.edit()
                .putLong(NOTIFICATION_DISABLE_POPUP_PREF_KEY, System.currentTimeMillis())
                .apply();
    }

    @Override
    public void callOnUi() {
        showPopup();
    }

    @Override
    public boolean isApplicable() {
        if (!getMarketApiManager().isPopupAvailable()) {

            final SharedPreferences preferences = App.getContext().getSharedPreferences(
                    Static.PREFERENCES_TAG_SHARED,
                    Context.MODE_PRIVATE
            );
            long date_start = preferences.getLong(NOTIFICATION_DISABLE_POPUP_PREF_KEY, 1);
            long date_now = System.currentTimeMillis();
            long delay = mActivity.getResources().getInteger(R.integer.notifications_disable_popup_delay) * 1000 * 60 * 60;
            if ((date_now - date_start) >= delay) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getPriority() {
        return StartActionsController.AC_PRIORITY_HIGH;
    }

    @Override
    public String getActionName() {
        return null;
    }

    private MarketApiManager getMarketApiManager() {
        if (mMarketApiManager == null) {
            mMarketApiManager = new MarketApiManager(mActivity);
        }
        return mMarketApiManager;
    }
}
