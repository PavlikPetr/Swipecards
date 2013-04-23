package com.topface.topface.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;


public class PopupManager {
    public static final String RATING_POPUP = "RATING_POPUP";
    public static final int RATE_POPUP_TIMEOUT = 86400000; // 1000 * 60 * 60 * 24 * 1 (1 сутки)
    public static final int INVITE_POPUP_TIMEOUT = 1;//604800000;
    private static boolean CAN_SHOW_POPUP = true;

    Context mContext;
    private boolean mRatingPopupIsShowing = false;

    public PopupManager(Context context) {
        mContext = context;
    }

    private boolean checkVersion(String version) {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
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

    public void showOldVersionPopup(String version) {
        if (checkVersion(version)) {
            CAN_SHOW_POPUP = false;
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
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    CAN_SHOW_POPUP = true;
                }
            });
            builder.create().show();
        }
    }

    public void showRatePopup() {
        if (!checkVersion(CacheProfile.getOptions().max_version) && App.isOnline() && mRatingPopupIsShowing) {
            ratingPopup();
        }
    }

    private void ratingPopup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences preferences = mContext.getSharedPreferences(
                        Static.PREFERENCES_TAG_SHARED,
                        Context.MODE_PRIVATE
                );

                long date_start = preferences.getLong(RATING_POPUP, 1);
                long date_now = new java.util.Date().getTime();

                if (date_start == 0 || (date_now - date_start < RATE_POPUP_TIMEOUT)) {
                    return;
                } else if (date_start == 1) {
                    saveRatingPopupStatus(new java.util.Date().getTime());
                    return;
                }

                Looper.prepare();
                getDialog().show();
                mRatingPopupIsShowing = true;
                Looper.loop();
            }
        }).start();

    }

    private Dialog getDialog() {
        CAN_SHOW_POPUP = false;
        final Dialog ratingPopup = new Dialog(mContext) {
            @Override
            public void onBackPressed() {
                saveRatingPopupStatus(new java.util.Date().getTime());
                super.onBackPressed();
            }
        };
        ratingPopup.setTitle(R.string.dashbrd_popup_title);
        ratingPopup.setContentView(R.layout.popup_rating);
        ratingPopup.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mRatingPopupIsShowing = false;
                CAN_SHOW_POPUP = true;
            }
        });

        ratingPopup.findViewById(R.id.btnRatingPopupRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToMarket(mContext);
                saveRatingPopupStatus(0);
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupLate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRatingPopupStatus(new java.util.Date().getTime());
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRatingPopupStatus(0);
                ratingPopup.cancel();
            }

        });
        return ratingPopup;
    }

    private void saveRatingPopupStatus(final long value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences.Editor editor = mContext.getSharedPreferences(
                        Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE
                ).edit();
                editor.putLong(RATING_POPUP, value);
                editor.commit();
            }
        }).start();
    }


}
