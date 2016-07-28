package com.topface.topface.ui.dialogs;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.databinding.PopularUserDialogBinding;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.viewModels.PopularUserDialogViewModel;

import static com.topface.topface.viewModels.PopularUserDialogViewModel.BLOCK_TEXT_ARG;
import static com.topface.topface.viewModels.PopularUserDialogViewModel.DIALOG_TITLE_ARG;

public class PopularUserDialog extends AbstractDialogFragment {

    private PopularUserDialogViewModel mViewModel;

    public static PopularUserDialog newInstance(String dialogTitle, String blockText) {
        PopularUserDialog fragment = new PopularUserDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE_ARG, dialogTitle);
        args.putString(BLOCK_TEXT_ARG, blockText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initViews(View root) {
        PopularUserDialogBinding binding = DataBindingUtil.bind(root);
        mViewModel = new PopularUserDialogViewModel(binding, getArguments(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = PurchasesActivity.createVipBuyIntent(null, "PopularUserBlockDialog");
                startActivity(intent);
                getDialog().dismiss();
            }
        });
        binding.setViewModel(mViewModel);
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
    protected int getDialogLayoutRes() {
        return R.layout.popular_user_dialog;
    }

    public void release() {
        if (mViewModel != null) {
            mViewModel.release();
            mViewModel = null;
        }
    }
}
