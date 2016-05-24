package com.topface.topface.utils;

import android.content.DialogInterface;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.ui.IDialogListener;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.dialogs.OldVersionDialog;
import com.topface.topface.ui.dialogs.RateAppDialog;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;


public class PopupManager {
    private IActivityDelegate mActivityDelegate;
    private AbstractDialogFragment mCurrentDialog;
    private OnNextActionListener mOldVersionPopupNextActionListener;
    private OnNextActionListener mRatePopupNextActionListener;

    public PopupManager(IActivityDelegate activityDelegate) {
        mActivityDelegate = activityDelegate;
    }

    public IStartAction createOldVersionPopupStartAction(final int priority) {
        return new IStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                final OldVersionDialog oldVersionDialog = OldVersionDialog.newInstance(true);
                oldVersionDialog.setDialogInterface(new IDialogListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Utils.goToMarket(mActivityDelegate, null);
                    }

                    @Override
                    public void onNegativeButtonClick() {
                        oldVersionDialog.getDialog().cancel();
                    }

                    @Override
                    public void onDismissListener() {
                        if (mOldVersionPopupNextActionListener != null) {
                            mOldVersionPopupNextActionListener.onNextAction();
                        }
                    }
                });
                oldVersionDialog.show(mActivityDelegate.getSupportFragmentManager(), OldVersionDialog.class.getName());
            }

            @Override
            public boolean isApplicable() {
                return isOldVersion(App.get().getOptions().maxVersion);
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "OldVersionPopup";
            }

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {
                mOldVersionPopupNextActionListener = startActionCallback;
            }
        };
    }

    private boolean isOldVersion(String version) {
        try {
            String curVersion = BuildConfig.VERSION_NAME;
            if (!TextUtils.isEmpty(version) && !TextUtils.isEmpty(curVersion)) {
                String[] splittedVersion = TextUtils.split(version, "\\.");
                String[] splittedCurVersion = TextUtils.split(curVersion, "\\.");
                for (int i = 0; i < splittedVersion.length; i++) {
                    if (i < splittedCurVersion.length) {
                        long curVersionLong = Long.parseLong(splittedCurVersion[i]);
                        long maxVersionLong = Long.parseLong(splittedVersion[i]);
                        if (curVersionLong < maxVersionLong) {
                            return true;
                        } else if (curVersionLong > maxVersionLong) {
                            return false;
                        }
                    }
                }
                if (splittedCurVersion.length < splittedVersion.length) {
                    return true;
                }
            }
        } catch (Exception e) {
            Debug.error("Check Version Error: " + version, e);

        }
        return false;
    }

    public IStartAction createRatePopupStartAction(final int priority, final long ratePopupTimeout, final boolean ratePopupEnabled) {
        return new IStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                showRatePopup();
            }

            @Override
            public boolean isApplicable() {
                return App.isOnline() && RateAppDialog.isApplicable(ratePopupTimeout, ratePopupEnabled) &&
                        !isOldVersion(App.get().getOptions().maxVersion);
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "RateAppPopup";
            }

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {
                mRatePopupNextActionListener = startActionCallback;
            }
        };
    }

    private void showRatePopup() {
        RateAppDialog rateAppDialog = new RateAppDialog();
        rateAppDialog.show(mActivityDelegate.getSupportFragmentManager(), RateAppDialog.TAG);
        rateAppDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mCurrentDialog = null;
                if (mRatePopupNextActionListener != null) {
                    mRatePopupNextActionListener.onNextAction();
                }
            }
        });
        mCurrentDialog = rateAppDialog;
    }

    public AbstractDialogFragment getCurrentDialog() {
        return mCurrentDialog;
    }

    public void onDestroy() {
        mActivityDelegate = null;
        mOldVersionPopupNextActionListener = null;
        mRatePopupNextActionListener = null;
    }
}
