package com.topface.topface.ui.dialogs;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.utils.CacheProfile;

public class PopularUserDialog extends AbstractModalDialog {
    private String mUserName;
    private int mUserSex;
    private TextView mTitle;
    private TextView mMessage;

    public PopularUserDialog(String userName, int userSex) {
        mUserName = userName;
        mUserSex = userSex;
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
                EasyTracker.getTracker().sendEvent(getTrackName(), "BuyVipStatus", "", 1L);
                Intent intent = ContainerActivity.getVipBuyIntent(null, "PopularUserBlockDialog");
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                getDialog().dismiss();
            }
        });
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.popular_user_dialog;
    }

    @Override
    protected void onCloseButtonClick(View v) {
        getDialog().dismiss();
    }
}
