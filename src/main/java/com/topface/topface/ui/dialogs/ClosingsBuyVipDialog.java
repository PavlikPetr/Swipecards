package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.ResourcesUtils;

public class ClosingsBuyVipDialog extends AbstractModalDialog implements View.OnClickListener {

    public static final String TAG = "com.topface.topface.ui.dialogs.ClosingsBuyVipDialog_TAG";
    private static final String ARG_FRAGMENT = "fragmentId";
    public static boolean opened = false;
    private IRespondToLikesListener mWatchSequentialyListener;

    public static ClosingsBuyVipDialog newInstance(BaseFragment.FragmentId fragmentId) {
        ClosingsBuyVipDialog dialog = new ClosingsBuyVipDialog();

        Bundle args = new Bundle();
        args.putSerializable(ARG_FRAGMENT, fragmentId);
        dialog.setArguments(args);

        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ClosingsBuyVipDialog.opened = false;
    }

    @Override
    public void initContentViews(View root) {
        getDialog().setCanceledOnTouchOutside(false);
        String fragmentName = Static.EMPTY;
        if (getActivity() != null) {
            BaseFragment.FragmentId fragmentId = (BaseFragment.FragmentId) getArguments().getSerializable(ARG_FRAGMENT);
            if (fragmentId != null) {
                fragmentName = getString(ResourcesUtils.getFragmentNameResId(fragmentId));
            }
        }
        root.findViewById(R.id.btnRespondToLikes).setOnClickListener(this);
        root.findViewById(R.id.btnBuyVip).setOnClickListener(this);
        ((TextView) root.findViewById(R.id.tvTitle))
                .setText(String.format(getString(R.string.locking_popup_title), fragmentName));
        ((TextView) root.findViewById(R.id.tvMessage))
                .setText(getString(R.string.locking_popup_message));
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.dialog_closings_buy_vip;
    }

    @Override
    protected void onCloseButtonClick(View v) {
        EasyTracker.getTracker().sendEvent(getTrackName(), "Close", "", 1L);
        closeDialog();
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

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        ClosingsBuyVipDialog.opened = true;
    }

    public void setOnRespondToLikesListener(IRespondToLikesListener listener) {
        mWatchSequentialyListener = listener;
    }

    @Override
    protected String getTrackName() {
        return "ClosingBuyVipPopup";
    }

    public interface IRespondToLikesListener {
        void onRespondToLikes();
    }
}
