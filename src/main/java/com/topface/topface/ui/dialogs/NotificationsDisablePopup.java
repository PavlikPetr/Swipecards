package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

public class NotificationsDisablePopup implements IStartAction {
    private Activity mActivity;
    private int mPriority;
    private OnNextActionListener mOnNextActionListener;

    public NotificationsDisablePopup(Activity activity, int priority) {
        mActivity = activity;
        mPriority = priority;
    }

    private MarketApiManager mMarketApiManager;

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
                    getMarketApiManager().onProblemResolve(mActivity);
                    dialog.cancel();
                    mActivity = null;
                }
            });
        }
        return builder.create();
    }

    @Override
    public void callInBackground() {
        App.getAppConfig().setTimeNotificationsDisabledShowAtLast(System.currentTimeMillis());
        App.getAppConfig().saveConfig();
    }

    @Override
    public void callOnUi() {
        AlertDialog notificationDisableDialog = getPopup();
        notificationDisableDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mOnNextActionListener != null) {
                    mOnNextActionListener.onNextAction();
                }
            }
        });
        notificationDisableDialog.show();
    }

    @Override
    public boolean isApplicable() {
        if (!getMarketApiManager().isMarketApiAvailable() && getMarketApiManager().isMarketApiSupportByUs()) {
            long date_now = System.currentTimeMillis();
            long delay = App.getContext().getResources().getInteger(R.integer.notifications_disable_popup_delay) * 1000;
            if ((date_now - App.getAppConfig().getTimeNotificationsDisabledShowAtLast()) >= delay) {
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

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        mOnNextActionListener = startActionCallback;
    }

    private MarketApiManager getMarketApiManager() {
        if (mMarketApiManager == null) {
            mMarketApiManager = new MarketApiManager();
        }
        return mMarketApiManager;
    }
}
