package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
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
import com.topface.topface.Static;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.ResourcesUtils;

public class ClosingsBuyVipDialog extends BaseDialogFragment implements View.OnClickListener {

    public static boolean opened = false;

    public static final String TAG = "com.topface.topface.ui.dialogs.ClosingsBuyVipDialog_TAG";
    private static final String ARG_FRAGMENT = "fragmentId";
    private IRespondToLikesListener mWatchSequentialyListener;
    private IWatchListListener mWatchListListener;


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ClosingsBuyVipDialog.opened = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_closings_buy_vip, container, false);

        String fragmentName = Static.EMPTY;
        if (getActivity() != null) {
            BaseFragment.FragmentId fragmentId = (BaseFragment.FragmentId) getArguments().getSerializable(ARG_FRAGMENT);
            fragmentName = getString(ResourcesUtils.getFragmentNameResId(fragmentId));
        }

        setTransparentBackground();
        getDialog().setCanceledOnTouchOutside(false);

        root.findViewById(R.id.btnRespondToLikes).setOnClickListener(this);
        root.findViewById(R.id.btnBuyVip).setOnClickListener(this);


        ((TextView) root.findViewById(R.id.tvTitle))
                .setText(String.format(getString(R.string.locking_popup_title), fragmentName));
        ((TextView) root.findViewById(R.id.tvMessage))
                .setText(String.format(getString(R.string.locking_popup_message), fragmentName));
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
            case R.id.btnRespondToLikes:
                EasyTracker.getTracker().sendEvent(getTrackName(), "AnswerOnLikes", "", 1L);
                if (mWatchSequentialyListener != null) mWatchSequentialyListener.onRespondToLikes();
                closeDialog();
                break;
            case R.id.btnBuyVip:
                EasyTracker.getTracker().sendEvent(getTrackName(), "BuyVipStatus", "", 1L);
                Intent intent = ContainerActivity.getVipBuyIntent(null, "ClosingDialogWatchAsList");
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                if (mWatchListListener != null) mWatchListListener.onWatchList();
                closeDialog();
                break;
            case R.id.btnClose:
                EasyTracker.getTracker().sendEvent(getTrackName(), "Close", "", 1L);
                closeDialog();
                break;
            default:
                break;
        }
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
    }

    public static ClosingsBuyVipDialog newInstance(BaseFragment.FragmentId fragmentId) {
        ClosingsBuyVipDialog dialog = new ClosingsBuyVipDialog();

        Bundle args = new Bundle();
        args.putSerializable(ARG_FRAGMENT, fragmentId);
        dialog.setArguments(args);

        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        ClosingsBuyVipDialog.opened = true;
    }

    public void setOnRespondToLikesListener(IRespondToLikesListener listener) {
        mWatchSequentialyListener = listener;
    }

    public void setOnWatchListListener(IWatchListListener listener) {
        mWatchListListener = listener;
    }

    public interface IRespondToLikesListener {
        void onRespondToLikes();
    }

    public interface IWatchListListener {
        void onWatchList();
    }

    @Override
    protected String getTrackName() {
        return "ClosingBuyVipPopup";
    }
}
