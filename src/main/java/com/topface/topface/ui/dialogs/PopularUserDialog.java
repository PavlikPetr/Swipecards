package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.statistics.PushButtonVipStatistics;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.EasyTracker;

public class PopularUserDialog extends AbstractModalDialog {

    private static final String DIALOG_TITLE_ARG = "DIALOG_TITLE_ARG";
    private static final String BLOCK_TEXT_ARG = "BLOCK_TEXT_ARG";
    private static final String IS_OPENED = "IS_OPENED";

    private String mDialogTitle;
    private String mBlockText;
    private boolean isOpened;

    public static PopularUserDialog newInstance(String dialogTitle, String blockText) {
        PopularUserDialog fragment = new PopularUserDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE_ARG, dialogTitle);
        args.putString(BLOCK_TEXT_ARG, blockText);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mDialogTitle = args.getString(DIALOG_TITLE_ARG);
            mBlockText = args.getString(BLOCK_TEXT_ARG);
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
        TextView title = (TextView) root.findViewById(R.id.popular_user_title);
        TextView message = (TextView) root.findViewById(R.id.popular_user_message);

        title.setText(mDialogTitle);
        message.setText(mBlockText);

        root.findViewById(R.id.unlock_message_sent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpened = false;
                PushButtonVipStatistics.sendPushButtonVip();
                EasyTracker.sendEvent(getTrackName(), "BuyVipStatus", "", 1L);
                Intent intent = PurchasesActivity.createVipBuyIntent(null, "PopularUserBlockDialog");
                startActivity(intent);
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
