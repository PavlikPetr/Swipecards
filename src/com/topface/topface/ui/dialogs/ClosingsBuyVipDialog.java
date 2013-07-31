package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileDelete;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClosingsBuyVipDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "com.topface.topface.ui.dialogs.ClosingsBuyVipDialog_TAG";
    private static final String ARG_LIKES = "likesCount";


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
        ((TextView) root.findViewById(R.id.idYouWasLiked)).setText(Utils.getQuantityString(R.plurals.you_was_liked,likesCount,likesCount));
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
            case R.id.btnClose:
                closeDialog();
                break;
            case R.id.btnWatchAsList:
                Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                break;
            default:
                break;
        }
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
    }

    public static ClosingsBuyVipDialog newInstance(int likesCount) {
        ClosingsBuyVipDialog dialog = new ClosingsBuyVipDialog();

        Bundle args = new Bundle();
        args.putInt(ARG_LIKES, likesCount);
        dialog.setArguments(args);

        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }
}
