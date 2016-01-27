package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.buy.TransparentMarketFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.config.UserConfig;

import java.lang.ref.WeakReference;


public class TrialVipPopupAction implements IStartAction {

    private int mPriority;
    private WeakReference<BaseFragmentActivity> mActivity;
    private TrialVipPopup mTrialVipPopup;
    private OnNextActionListener mOnNextActionListener;

    public TrialVipPopupAction(BaseFragmentActivity activity, int priority) {
        mActivity = new WeakReference<>(activity);
        mPriority = priority;
    }

    @Override
    public void callInBackground() {
    }

    @Override
    public void callOnUi() {
        mTrialVipPopup = TrialVipPopup.newInstance(false);
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
        if (mActivity != null && mActivity.get() != null) {
            mTrialVipPopup.show(mActivity.get().getSupportFragmentManager(), TrialVipPopup.TAG);
            UserConfig userConfig = App.getUserConfig();
            userConfig.setTrialLastTime(System.currentTimeMillis());
            userConfig.saveConfig();
        }
    }

    @Override
    public boolean isApplicable() {
        UserConfig userConfig = App.getUserConfig();
        if (DateUtils.isDayBeforeToday(userConfig.getTrialLastTime())) {
            userConfig.setTrialVipPopupCounter(UserConfig.DEFAULT_SHOW_COUNT);
            userConfig.saveConfig();
        }
        return !CacheProfile.paid && !CacheProfile.premium &&
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
        if (mActivity != null && mActivity.get() != null) {
            Fragment f = mActivity.get().getSupportFragmentManager().findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
            final TransparentMarketFragment fragment = f == null ?
                    TransparentMarketFragment.newInstance(CacheProfile.getOptions().trialVipExperiment.subscriptionSku, true, "TrialVipPopup") :
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
            mActivity.get().getSupportFragmentManager().beginTransaction()
                    .add(fragment, TransparentMarketFragment.class.getSimpleName()).commit();
        }
    }

    private void removeTransparentMarketFragment(Fragment fragment) {
        if (isFragmentAplicable()) {
            mActivity.get().getSupportFragmentManager().
                    beginTransaction().remove(fragment).commit();
        }
    }

    private boolean isFragmentAplicable() {
        return mActivity != null && mActivity.get() != null && mActivity.get().isActivityRestoredState();
    }
}
