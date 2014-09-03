package com.topface.topface.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.dialogs.AbstractModalDialog;
import com.topface.topface.ui.dialogs.InvitesPopup;
import com.topface.topface.ui.dialogs.RateAppDialog;
import com.topface.topface.utils.controllers.AbstractStartAction;
import com.topface.topface.utils.controllers.IStartAction;

import java.util.ArrayList;


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
                startOldVersionPopup();
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

    private void startOldVersionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setPositiveButton(R.string.popup_version_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.goToMarket(mActivity);
            }
        });
        builder.setNegativeButton(R.string.popup_version_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setMessage(R.string.general_version_not_supported);
        builder.create().show();
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

    public IStartAction createInvitePopupStartAction(final int priority) {
        return new AbstractStartAction() {
            @Override
            public void callInBackground() {
                SharedPreferences preferences = App.getContext().getSharedPreferences(
                        Static.PREFERENCES_TAG_SHARED,
                        Context.MODE_PRIVATE
                );
                preferences.edit()
                        .putLong(InvitesPopup.INVITE_POPUP_PREF_KEY, System.currentTimeMillis())
                        .apply();
            }

            @Override
            public void callOnUi() {
                startInvitePopup();
            }

            @Override
            public boolean isApplicable() {
                return InvitesPopup.isApplicable();
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "InviteContactsPopup";
            }
        };
    }

    private void startInvitePopup() {
        ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
            @Override
            public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {

                InvitesPopup popup = InvitesPopup.newInstance(contacts);
                popup.show(mActivity.getSupportFragmentManager(), InvitesPopup.TAG);
                mCurrentDialog = popup;
                popup.onDismiss(new DialogInterface() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void dismiss() {
                        mCurrentDialog = null;
                    }
                });
                EasyTracker.sendEvent("InvitesPopup", "Show", "", 0L);

            }
        };

        ContactsProvider provider = new ContactsProvider(mActivity);
        provider.getContacts(-1, 0, handler);
    }

    public AbstractDialogFragment getCurrentDialog() {
        return mCurrentDialog;
    }
}