package com.topface.topface.utils;

import android.content.DialogInterface;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.dialogs.RateAppDialog;
import com.topface.topface.utils.controllers.AbstractStartAction;
import com.topface.topface.utils.controllers.IStartAction;


public class PopupManager {
    private BaseFragmentActivity mActivity;

    private AbstractDialogFragment mCurrentDialog;

    public PopupManager(BaseFragmentActivity activity) {
        mActivity = activity;
    }

    public IStartAction createOldVersionPopupStartAction(final int priority) {
        return new AbstractStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                Utils.startOldVersionPopup(mActivity);
            }

            @Override
            public boolean isApplicable() {
                return isOldVersion(CacheProfile.getOptions().maxVersion);
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "OldVersionPopup";
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

    public IStartAction createRatePopupStartAction(final int priority) {
        return new AbstractStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                showRatePopup();
            }

            @Override
            public boolean isApplicable() {
                return App.isOnline() && RateAppDialog.isApplicable() &&
                        !isOldVersion(CacheProfile.getOptions().maxVersion);
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "RateAppPopup";
            }
        };
    }

    private void showRatePopup() {
        RateAppDialog rateAppDialog = new RateAppDialog();
        rateAppDialog.show(mActivity.getSupportFragmentManager(), RateAppDialog.TAG);
        rateAppDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mCurrentDialog = null;
            }
        });
        mCurrentDialog = rateAppDialog;
    }

    public AbstractDialogFragment getCurrentDialog() {
        return mCurrentDialog;
    }
}
