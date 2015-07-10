package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.TransparentMarketFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.GoogleMarketApiManager;


public class TrialVipPopupAction implements IStartAction {

    private int mPriority;
    private BaseFragmentActivity mActivity;
    private TrialVipPopup mTrialVipPopup;
    private OnNextActionListener mOnNextActionListener;

    public TrialVipPopupAction(BaseFragmentActivity activity, int priority) {
        mActivity = activity;
        mPriority = priority;
    }

    @Override
    public void callInBackground() {
    }

    @Override
    public void callOnUi() {
        mTrialVipPopup = new TrialVipPopup();
        mTrialVipPopup.setOnSubscribe(new TrialVipPopup.OnFragmentActionsListener() {
            @Override
            public void onSubscribeClick() {
                showSubscriptionPopup();
            }

            @Override
            public void onFragmentFinish() {
                if (mOnNextActionListener != null) {
                    mOnNextActionListener.onNextAction();
                }
            }
        });
        if (mActivity != null) {
            mTrialVipPopup.show(mActivity.getSupportFragmentManager(), TrialVipPopup.TAG);
        }
    }

    @Override
    public boolean isApplicable() {
        return !CacheProfile.paid &&
                App.getUserConfig().getTrialVipCounter() < CacheProfile.getOptions().getMaxShowCountTrialVipPopup() &&
                CacheProfile.getOptions().trialVipExperiment.enabled && new GoogleMarketApiManager().isMarketApiAvailable();
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
        mOnNextActionListener = startActionCallback;
    }

    private void showSubscriptionPopup() {
        if (mActivity != null) {
            Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
            final TransparentMarketFragment fragment = f == null ?
                    TransparentMarketFragment.newInstance(CacheProfile.getOptions().trialVipExperiment.subscriptionSku, true) :
                    (TransparentMarketFragment) f;
            fragment.setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseActions() {
                @Override
                public void onPurchaseSuccess() {
                    if (null != mTrialVipPopup) {
                        mTrialVipPopup.dismiss();
                    }
                }

                @Override
                public void onPopupClosed() {
                    if (fragment.isAdded()) {
                        removeTransparentMarketFragment(fragment);
                    }
                }
            });
            if (!fragment.isAdded()) {
                addTransparentMarketFragment(fragment);
            } else {
                removeTransparentMarketFragment(fragment);
                addTransparentMarketFragment(fragment);
            }
        }
    }

    private void addTransparentMarketFragment(Fragment fragment) {
        if (isFragmentAplicable()) {
            mActivity.getSupportFragmentManager().beginTransaction()
                    .add(fragment, TransparentMarketFragment.class.getSimpleName()).commit();
        }
    }

    private void removeTransparentMarketFragment(Fragment fragment) {
        if (isFragmentAplicable()) {
            mActivity.getSupportFragmentManager().
                    beginTransaction().remove(fragment).commit();
        }
    }

    private boolean isFragmentAplicable() {
        return mActivity != null && mActivity.isActivityRestoredState();
    }
}
