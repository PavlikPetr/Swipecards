package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.IDialogListener;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

public class NotificationsDisablePopup implements IStartAction {
    private FragmentActivity mActivity;
    private int mPriority;
    private OnNextActionListener mOnNextActionListener;

    public NotificationsDisablePopup(FragmentActivity activity, int priority) {
        mPriority = priority;
        mActivity = activity;
    }

    private MarketApiManager mMarketApiManager;

    private NotificationDisableDialog getPopup() {
        return NotificationDisableDialog.newInstance(getMarketApiManager().getTitleTextId(), getMarketApiManager().getButtonTextId(), getMarketApiManager().isButtonVisible());
    }

    @Override
    public void callInBackground() {
        App.getAppConfig().setTimeNotificationsDisabledShowAtLast(System.currentTimeMillis());
        App.getAppConfig().saveConfig();
    }

    @Override
    public void callOnUi() {
        final NotificationDisableDialog notificationDisableDialog = getPopup();
        notificationDisableDialog.setDialogInterface(new IDialogListener() {
            @Override
            public void onPositiveButtonClick() {
                getMarketApiManager().onProblemResolve(mActivity);
                notificationDisableDialog.dismiss();
                mActivity = null;
            }

            @Override
            public void onNegativeButtonClick() {
                notificationDisableDialog.getDialog().cancel();
                mActivity = null;
            }

            @Override
            public void onDismissListener() {
                if (mOnNextActionListener != null) {
                    mOnNextActionListener.onNextAction();
                }
            }
        });
        if (mActivity != null) {
            notificationDisableDialog.show(mActivity.getSupportFragmentManager(), NotificationDisableDialog.class.getName());
        }
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

    public static class NotificationDisableDialog extends BaseDialog {

        private static final String MARKET_TITLE_TEXT_ID = "market_title_text_id";
        private static final String MARKET_BUTTON_TEXT_ID = "market_button_text_id";
        private static final String IS_MARKET_BUTTON_VISIBLE = "is_market_button_visible";

        private IDialogListener mIDialogListener;

        public static NotificationDisableDialog newInstance(int titleId, int buttonTextId, boolean isButtonVisible) {
            NotificationDisableDialog dialog = new NotificationDisableDialog();
            Bundle bundle = new Bundle();
            bundle.putInt(MARKET_TITLE_TEXT_ID, titleId);
            bundle.putInt(MARKET_BUTTON_TEXT_ID, buttonTextId);
            bundle.putBoolean(IS_MARKET_BUTTON_VISIBLE, isButtonVisible);
            dialog.setArguments(bundle);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int titleId = 0;
            int buttonTextId = 0;
            boolean isButtonVisible = false;
            Bundle bundle = getArguments();
            if (bundle != null) {
                titleId = bundle.getInt(MARKET_TITLE_TEXT_ID);
                buttonTextId = bundle.getInt(MARKET_BUTTON_TEXT_ID);
                isButtonVisible = bundle.getBoolean(IS_MARKET_BUTTON_VISIBLE);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.google_service_general_title).setMessage(titleId)
                    .setCancelable(true)
                    .setNegativeButton(getActivity().getResources().getString(R.string.general_cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (mIDialogListener != null) {
                                        mIDialogListener.onNegativeButtonClick();
                                    }
                                }
                            });
            if (isButtonVisible) {
                builder.setPositiveButton(buttonTextId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mIDialogListener != null) {
                            mIDialogListener.onPositiveButtonClick();
                        }
                    }
                });
            }
            return builder.create();
        }

        public void setDialogInterface(IDialogListener dialogInterface) {
            mIDialogListener = dialogInterface;
        }

        @Override
        protected void initViews(View root) {

        }

        @Override
        protected int getDialogLayoutRes() {
            return 0;
        }

        @Override
        protected int getDialogStyleResId() {
            return 0;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (mIDialogListener != null) {
                mIDialogListener.onDismissListener();
            }
        }
    }
}
