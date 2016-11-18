package com.topface.topface.ui.dialogs.trial_vip_experiment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.buy.TransparentMarketFragment;
import com.topface.topface.ui.views.ITransparentMarketFragmentRunner;

import java.lang.ref.WeakReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Показывалка попапа покупок. Котлин не дружит с флейворами
 * Created by tiberal on 16.11.16.
 */

public class TransparentMarketFragmentRunner {

    private WeakReference<FragmentActivity> mActivity;

    public TransparentMarketFragmentRunner(FragmentActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public void startTransparentMarketFragment(final Function0<Unit> function) {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }
        Fragment f = mActivity.get().getSupportFragmentManager().findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
        final Fragment fragment = f == null ?
                TransparentMarketFragment.newInstance(App.get().getOptions().trialVipExperiment.subscriptionSku, true, TrialVipPopup.TAG) : f;
        fragment.setRetainInstance(true);
        if (fragment instanceof ITransparentMarketFragmentRunner) {
            ((ITransparentMarketFragmentRunner) fragment).setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseActions() {
                @Override
                public void onPurchaseSuccess() {
                    function.invoke();
                }

                @Override
                public void onPopupClosed() {
                }
            });
            FragmentTransaction transaction = mActivity.get().getSupportFragmentManager().beginTransaction();
            if (!fragment.isAdded()) {
                transaction.add(R.id.fragment_content, fragment, TransparentMarketFragment.class.getSimpleName()).commit();
            } else {
                transaction.remove(fragment)
                        .add(R.id.fragment_content, fragment, TransparentMarketFragment.class.getSimpleName()).commit();
            }
        }
    }

    public interface IRunner {
        void runMarketPopup();
    }
}
