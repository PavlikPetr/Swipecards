package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;

public class PopularUserDialog extends AbstractModalDialog {

    private static final String USER_NAME_ARG = "USER_NAME_ARG";
    private static final String USER_SEX_ARG = "USER_SEX_ARG";
    private static final String IS_OPENED = "IS_OPENED";

    private String mUserName;
    private int mUserSex;
    private TextView mTitle;
    private TextView mMessage;
    private boolean isOpened;

    public PopularUserDialog(String userName, int userSex) {
        mUserName = userName;
        mUserSex = userSex;
        Bundle args = new Bundle();
        args.putString(USER_NAME_ARG, mUserName);
        args.putInt(USER_SEX_ARG, mUserSex);
        setArguments(args);
    }

    public PopularUserDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mUserName = args.getString(USER_NAME_ARG);
            mUserSex = args.getInt(USER_SEX_ARG);
        }
        if (savedInstanceState != null) {
            isOpened = savedInstanceState.getBoolean(IS_OPENED, false);
        }
    }

    @Override
    public void onDestroyView() {
        Dialog d = getDialog();
        if (d != null && isOpened) {
            d.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    protected void initContentViews(View root) {
        mTitle = (TextView) root.findViewById(R.id.popular_user_title);
        mMessage = (TextView) root.findViewById(R.id.popular_user_message);

        Options options = CacheProfile.getOptions();

        mTitle.setText(options.popularUserLock.dialogTitle);
        mMessage.setText(mUserName + " " + (mUserSex == Static.BOY ?
                options.popularUserLock.maleLockText : options.popularUserLock.femaleLockText));

        root.findViewById(R.id.unlock_message_sent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpened = false;
                EasyTracker.sendEvent(getTrackName(), "BuyVipStatus", "", 1L);
                Intent intent = PurchasesActivity.createVipBuyIntent(null, "PopularUserBlockDialog");
                startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        isOpened = true;
        super.show(manager, tag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_OPENED, isOpened);
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.popular_user_dialog;
    }

    @Override
    protected void onCloseButtonClick(View v) {
        isOpened = false;
        Dialog d = getDialog();
        if (d != null) {
            d.dismiss();
        }
    }

    public boolean isOpened() {
        return isOpened;
    }
}
