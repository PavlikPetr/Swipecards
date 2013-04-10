package com.topface.topface.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;


public class PopupManager {
    Context mContext;

    public PopupManager(Context context) {
        mContext = context;
    }

    private boolean checkVersion(String version) {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String curVersion = pInfo.versionName;
            if (!TextUtils.isEmpty(version) && TextUtils.isEmpty(curVersion)) {
                String[] splittedVersion = version.split("\\.");
                String[] splittedCurVersion = curVersion.split("\\.");
                for (int i = 0; i < splittedVersion.length; i++) {
                    if (i < splittedCurVersion.length) {
                        if (Long.parseLong(splittedCurVersion[i]) < Long.parseLong(splittedVersion[i])) {
                            return true;
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

    public void showOldVersionPopup(String version) {
        if (checkVersion(version)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setPositiveButton(R.string.popup_version_update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.goToMarket(mContext);
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
    }
}
