package com.topface.topface.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.RateAppDialog;
import com.topface.topface.ui.fragments.InvitesPopup;
import com.topface.topface.utils.controllers.IStartAction;

import java.util.ArrayList;


public class PopupManager {
    public static final String OFF_RATE_TYPE = "OFF";
    public static final String STANDARD_RATE_TYPE = "STANDARD";
    public static final String LONG_RATE_TYPE = "LONG";

    private BaseFragmentActivity mActivity;

    public PopupManager(BaseFragmentActivity activity) {
        mActivity = activity;
    }

    public IStartAction createOldVersionPopupStartAction(final int priority) {
        return new IStartAction() {
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
            public String toDebugString() {
                return "StartAction:OldVersionPopup:" + getPriority() + ":" + isApplicable();
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
            PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            String curVersion = pInfo.versionName;
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
                return App.isOnline() && RateAppDialog.isApplicable() &&
                        !isOldVersion(CacheProfile.getOptions().maxVersion);
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String toDebugString() {
                return "StartAction:RateAppPopup:" + getPriority() + ":" + isApplicable();
            }
        };
    }

    private void showRatePopup() {
        RateAppDialog rateAppDialog = new RateAppDialog();
        rateAppDialog.show(mActivity.getSupportFragmentManager(), RateAppDialog.TAG);
    }

    public IStartAction createInvitePopupStartAction(final int priority) {
        return new IStartAction() {
            @Override
            public void callInBackground() {
                SharedPreferences preferences = App.getContext().getSharedPreferences(
                        Static.PREFERENCES_TAG_SHARED,
                        Context.MODE_PRIVATE
                );
                preferences.edit()
                        .putLong(InvitesPopup.INVITE_POPUP_PREF_KEY, System.currentTimeMillis())
                        .commit();
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
            public String toDebugString() {
                return "StartAction:InviteContactsPopup:" + getPriority() + ":" + isApplicable();
            }
        };
    }

    private void startInvitePopup() {
        ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
            @Override
            public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {
                EasyTracker.getTracker().sendEvent("InvitesPopup", "Show", "", 0L);
                InvitesPopup popup = InvitesPopup.newInstance(contacts);
                mActivity.startFragment(popup);
            }
        };

        ContactsProvider provider = new ContactsProvider(mActivity);
        provider.getContacts(-1, 0, handler);
    }
}