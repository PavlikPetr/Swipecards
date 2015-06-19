package com.topface.topface.utils.controllers.startactions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.TransparentMarketFragment;
import com.topface.topface.utils.CacheProfile;


public class TrialVipPopupAction extends DailyPopupAction {

    private int mPriority;
    private FragmentManager mFragmentManager;
    private TrialVipPopup mTrialVipPopup;

    public TrialVipPopupAction(FragmentManager fragmentManager, int priority) {
        super(App.getContext());
        mFragmentManager = fragmentManager;
        mPriority = priority;
    }

    @Override
    protected boolean firstStartShow() {
        return true;
    }

    @Override
    public void callInBackground() {
    }

    @Override
    public void callOnUi() {
        mTrialVipPopup = new TrialVipPopup();
        mTrialVipPopup.setOnSubscribe(new TrialVipPopup.OnSubscribe() {
            @Override
            public void onClick() {
                showSubscriptionPopup();
            }
        });
        mTrialVipPopup.show(mFragmentManager, TrialVipPopup.TAG);
    }

    @Override
    public boolean isApplicable() {
        return !CacheProfile.paid &&
                App.getUserConfig().getTrialVipCounter() < CacheProfile.getOptions().trialVipExperiment.maxShowCount &&
                CacheProfile.getOptions().trialVipExperiment.enable;
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {

    }

    private void showSubscriptionPopup() {
        Fragment f = mFragmentManager.findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
        final TransparentMarketFragment fragment = f == null ? new TransparentMarketFragment() : (TransparentMarketFragment) f;
        Bundle bundle = new Bundle();
        bundle.putString(TransparentMarketFragment.SUBSCRIPTION_ID, CacheProfile.getOptions().trialVipExperiment.subscriptionSku);
        bundle.putBoolean(TransparentMarketFragment.IS_SUBSCRIPTION, true);
        fragment.setArguments(bundle);
        fragment.setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseCompleteAction() {
            @Override
            public void onPurchaseAction() {
                if (null != mTrialVipPopup) {
                    mTrialVipPopup.dismiss();
                }
                if (fragment.isAdded()) {
                    mFragmentManager.
                            beginTransaction().remove(fragment).commit();
                }
            }
        });
        mFragmentManager.beginTransaction()
                .add(fragment, TransparentMarketFragment.class.getSimpleName()).commit();
    }
}
