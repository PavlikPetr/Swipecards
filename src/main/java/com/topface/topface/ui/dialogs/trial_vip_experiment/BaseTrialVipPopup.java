package com.topface.topface.ui.dialogs.trial_vip_experiment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.ui.fragments.buy.TransparentMarketFragment;
import com.topface.topface.ui.views.ITransparentMarketFragmentRunner;
import com.topface.topface.utils.config.UserConfig;

import org.jetbrains.annotations.NotNull;

/**
 * Базовый класс для триальных випов
 * Created by siberia87 on 15.11.16.
 */

abstract public class BaseTrialVipPopup extends AbstractDialogFragment implements View.OnClickListener {

    protected @Nullable IOnFragmentFinishDelegate mOnFragmentFinishDelegate;
    public static final String SKIP_SHOWING_CONDITION = "skip_showing_condition";

    public void setOnFragmentFinishDelegate(final @NotNull IOnFragmentFinishDelegate delegate) {
        mOnFragmentFinishDelegate = delegate;
    }

    public void onVipTrialPurchaseStart() {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
        final Fragment fragment = f == null ?
                TransparentMarketFragment.newInstance(App.get().getOptions().trialVipExperiment.subscriptionSku, true, TrialVipPopup.TAG) : f;
        fragment.setRetainInstance(true);
        if (fragment instanceof ITransparentMarketFragmentRunner) {
            ((ITransparentMarketFragmentRunner) fragment).setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseActions() {
                @Override
                public void onPurchaseSuccess() {
                    dismiss();
                }

                @Override
                public void onPopupClosed() {
                }
            });
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            if (!fragment.isAdded()) {
                transaction.add(R.id.fragment_content, fragment, TransparentMarketFragment.class.getSimpleName()).commit();
            } else {
                transaction.remove(fragment)
                        .add(R.id.fragment_content, fragment, TransparentMarketFragment.class.getSimpleName()).commit();
            }
        }
    }

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    public boolean isUnderActionBar() {
        return false;
    }

    public void onFragmentFinish() {
        if (mOnFragmentFinishDelegate != null) {
            mOnFragmentFinishDelegate.closeFragmentByForm();
        }
    }

    protected void incrPopupShowCounter() {
        UserConfig userConfig = App.getUserConfig();
        userConfig.setTrialVipPopupCounter(userConfig.getTrialVipCounter() + 1);
        userConfig.saveConfig();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Bundle args = getArguments();
        if (args != null && !args.getBoolean(SKIP_SHOWING_CONDITION)) {
            incrPopupShowCounter();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        onFragmentFinish();
    }
}
