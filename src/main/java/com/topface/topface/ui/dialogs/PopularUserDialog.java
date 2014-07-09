package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.ui.PurchasesActivity;

public class PopularUserDialog extends AbstractModalDialog {

    private static final String DIALOG_TITLE_ARG = "DIALOG_TITLE_ARG";
    private static final String BLOCK_TEXT_ARG = "BLOCK_TEXT_ARG";
    private static final String IS_OPENED = "IS_OPENED";

    private String mDialogTitle;
    private String mBlockText;
    private TextView mTitle;
    private TextView mMessage;
    private boolean isOpened;

    public PopularUserDialog(String dialogTitle, String blockText) {
        mDialogTitle = dialogTitle;
        mBlockText = blockText;
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE_ARG, mDialogTitle);
        args.putString(BLOCK_TEXT_ARG, mBlockText);
        setArguments(args);
    }

    public PopularUserDialog() {
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
        mTitle = (TextView) root.findViewById(R.id.popular_user_title);
        mMessage = (TextView) root.findViewById(R.id.popular_user_message);

        mTitle.setText(mDialogTitle);
        mMessage.setText(mBlockText);

        root.findViewById(R.id.unlock_message_sent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpened = false;
                EasyTracker.getTracker().sendEvent(getTrackName(), "BuyVipStatus", "", 1L);
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
