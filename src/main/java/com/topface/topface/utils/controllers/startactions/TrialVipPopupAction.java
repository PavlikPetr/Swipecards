package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.TransparentMarketFragment;
import com.topface.topface.utils.CacheProfile;


public class TrialVipPopupAction implements IStartAction {

    private int mPriority;
    private FragmentManager mFragmentManager;
    private TrialVipPopup mTrialVipPopup;
    private String mTag;
    private OnNextActionListener mOnNextActionListener;

    public TrialVipPopupAction(FragmentManager fragmentManager, int priority, String tag) {
        mFragmentManager = fragmentManager;
        mPriority = priority;
        mTag = tag;
    }

    @Override
    public void callInBackground() {
    }

    @Override
    public void callOnUi() {
        Debug.error("callOnUi tag " + mTag);
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
        mTrialVipPopup.show(mFragmentManager, TrialVipPopup.TAG);
    }

    @Override
    public boolean isApplicable() {
        return !CacheProfile.paid && !CacheProfile.premium &&
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
        mOnNextActionListener = startActionCallback;
    }

    private void showSubscriptionPopup() {
        Fragment f = mFragmentManager.findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
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

    private void addTransparentMarketFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .add(fragment, TransparentMarketFragment.class.getSimpleName()).commit();
    }

    private void removeTransparentMarketFragment(Fragment fragment) {
        mFragmentManager.
                beginTransaction().remove(fragment).commit();
    }
}
