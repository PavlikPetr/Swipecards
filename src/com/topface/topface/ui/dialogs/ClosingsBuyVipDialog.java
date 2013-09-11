package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.utils.Utils;

public class ClosingsBuyVipDialog extends BaseDialogFragment implements View.OnClickListener {

    public static boolean opened = false;

    public static final String TAG = "com.topface.topface.ui.dialogs.ClosingsBuyVipDialog_TAG";
    private static final String ARG_LIKES = "likesCount";
    private IWatchSequentialyListener mWatchSequentialyListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_closings_buy_vip, container, false);

        int likesCount = 0;
        if (getActivity() != null) {
            likesCount = getArguments().getInt(ARG_LIKES);
        }

        setTransparentBackground();
        getDialog().setCanceledOnTouchOutside(false);

        root.findViewById(R.id.btnWatchAsList).setOnClickListener(this);
        root.findViewById(R.id.btnWatchSequentually).setOnClickListener(this);
        ((TextView) root.findViewById(R.id.idYouWasLiked)).setText(Utils.getQuantityString(R.plurals.you_was_liked, likesCount, likesCount));
        root.findViewById(R.id.btnClose).setOnClickListener(this);
        return root;
    }

    private void setTransparentBackground() {
        ColorDrawable color = new ColorDrawable(Color.BLACK);
        color.setAlpha(175);
        getDialog().getWindow().setBackgroundDrawable(color);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnWatchSequentually:
                EasyTracker.getTracker().sendEvent(getTrackName(), "WatchSequentially", "", 1L);
                if (mWatchSequentialyListener != null) mWatchSequentialyListener.onWatchSequentialy(true);
                closeDialog();
                break;
            case R.id.btnClose:
                EasyTracker.getTracker().sendEvent(getTrackName(), "Close", "", 1L);
                closeDialog();
                break;
            case R.id.btnWatchAsList:
                EasyTracker.getTracker().sendEvent(getTrackName(), "WatchAsList", "", 1L);
                Intent intent = ContainerActivity.getVipBuyIntent(null, "ClosingDialogWatchAsList");
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                closeDialog();
                break;
            default:
                break;
        }
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
        ClosingsBuyVipDialog.opened = false;
    }

    public static ClosingsBuyVipDialog newInstance(int likesCount) {
        ClosingsBuyVipDialog dialog = new ClosingsBuyVipDialog();

        Bundle args = new Bundle();
        args.putInt(ARG_LIKES, likesCount);
        dialog.setArguments(args);

        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        ClosingsBuyVipDialog.opened = true;
    }

    public void setOnWatchSequentialyListener(IWatchSequentialyListener listener) {
        mWatchSequentialyListener = listener;
    }

    public interface IWatchSequentialyListener {
        void onWatchSequentialy();
    }

    @Override
    protected String getTrackName() {
        return "ClosingBuyVipPopup";
    }
}
