package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.topface.topface.R;


public class WrongTimePopup {
    public final static int ACTION_DATE_SETTINGS_INTENT_ID = 111;
    Activity mActivity;
    AlertDialog mPopup;

    public WrongTimePopup(Activity activity) {
        mActivity = activity;

    }

    private AlertDialog getAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getResources().getString(R.string.wrong_time_popup_title))
                .setMessage(mActivity.getResources().getString(R.string.wrong_time_popup_text))
                .setCancelable(false)
                .setPositiveButton(mActivity.getResources().getString(R.string.wrong_time_popup_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), ACTION_DATE_SETTINGS_INTENT_ID);
                            }
                        });
        return builder.create();
    }

    public void onDestroy() {
        mActivity = null;
    }

    public static boolean isCurrentTimeCorrect() {
        return false;
    }

    public void showPopup() {
        if (mPopup == null) {
            mPopup = getAlertDialog();
        }
        mPopup.show();
    }

    public void checkAndShowPopup() {
        if (!isCurrentTimeCorrect()) {
            showPopup();
        }
    }

}
