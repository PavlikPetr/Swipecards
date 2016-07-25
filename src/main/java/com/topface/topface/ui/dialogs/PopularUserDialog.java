package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.databinding.PopularUserDialogBinding;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.viewModels.PopularUserDialogViewModel;

import static com.topface.topface.viewModels.PopularUserDialogViewModel.BLOCK_TEXT_ARG;
import static com.topface.topface.viewModels.PopularUserDialogViewModel.DIALOG_TITLE_ARG;

public class PopularUserDialog extends AbstractDialogFragment {

    private static final String IS_OPENED = "IS_OPENED";

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
    protected void initViews(View root) {
        PopularUserDialogBinding mBinding = DataBindingUtil.bind(root);
        PopularUserDialogViewModel mViewModel = new PopularUserDialogViewModel(mBinding, getArguments(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOpened = false;
                Intent intent = PurchasesActivity.createVipBuyIntent(null, "PopularUserBlockDialog");
                startActivity(intent);
                getDialog().dismiss();
            }
        });
        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        isOpened = true;
        super.show(manager, tag);
    }

    @Override
    protected boolean isModalDialog() {
        return true;
    }

    @Override
    public boolean isUnderActionBar() {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_OPENED, isOpened);
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.popular_user_dialog;
    }

    public boolean isOpened() {
        return isOpened;
    }
}
