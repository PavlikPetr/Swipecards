package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.topface.topface.R;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.controllers.AbstractStartAction;

public class NotificationsDisablePopup extends AbstractStartAction {
    private Activity mActivity;
    private int mPriority;
    private AppConfig mAppConfig;

    public NotificationsDisablePopup(Activity activity, int priority) {
        mActivity = activity;
        mPriority = priority;
    }

    private MarketApiManager mMarketApiManager;

    private void showPopup() {
        getPopup().show();
    }

    private AlertDialog getPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.google_service_general_title).setMessage(getMarketApiManager().getTitleTextId())
                .setCancelable(true)
                .setNegativeButton(mActivity.getResources().getString(R.string.general_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mActivity = null;
                            }
                        });
        if (getMarketApiManager().isButtonVisible()) {
            builder.setPositiveButton(getMarketApiManager().getButtonTextId(), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    getMarketApiManager().onProblemResolve();
                    dialog.cancel();
                    mActivity = null;
                }
            });
        }
        return builder.create();
    }

    @Override
    public void callInBackground() {
        getAppConfig().setTimeNotificationsDisabledShowAtLast(System.currentTimeMillis());
    }

    @Override
    public void callOnUi() {
        showPopup();
    }

    @Override
    public boolean isApplicable() {
        if (!getMarketApiManager().isMarketApiAvailable() && getMarketApiManager().isMarketApiSupportByUs()) {
            long date_now = System.currentTimeMillis();
            long delay = mActivity.getResources().getInteger(R.integer.notifications_disable_popup_delay) * 1000;
            if ((date_now - getAppConfig().getTimeNotificationsDisabledShowAtLast()) >= delay) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return "NotificationsDisablePopup";
    }

    private MarketApiManager getMarketApiManager() {
        if (mMarketApiManager == null) {
            mMarketApiManager = new MarketApiManager();
        }
        return mMarketApiManager;
    }

    private AppConfig getAppConfig() {
        if (mAppConfig == null) {
            mAppConfig = new AppConfig(mActivity.getApplicationContext());
        }
        return mAppConfig;
    }
}
