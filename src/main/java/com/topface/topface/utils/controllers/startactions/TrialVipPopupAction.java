package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.buy.TransparentMarketFragment;
import com.topface.topface.ui.views.ITransparentMarketFragmentRunner;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.config.UserConfig;

import java.lang.ref.WeakReference;


public class TrialVipPopupAction implements IStartAction {

    private int mPriority;
    private WeakReference<BaseFragmentActivity> mActivity;
    private TrialVipPopup mTrialVipPopup;
    private OnNextActionListener mOnNextActionListener;
    private boolean mIsNeedNext = true;

    public TrialVipPopupAction(BaseFragmentActivity activity, int priority) {
        mActivity = new WeakReference<>(activity);
        mPriority = priority;
    }

    @Override
    public void callInBackground() {
    }

    @Override
    public void callOnUi() {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }
        mTrialVipPopup = (TrialVipPopup) mActivity.get().getSupportFragmentManager().findFragmentByTag(TrialVipPopup.TAG);
        if (mTrialVipPopup == null) {
            mTrialVipPopup = TrialVipPopup.newInstance(false);
        }
        mTrialVipPopup.setOnSubscribe(new TrialVipPopup.OnFragmentActionsListener() {
            @Override
            public void onSubscribeClick() {
                showSubscriptionPopup();
            }

            @Override
            public void onFragmentFinish() {
                if (mOnNextActionListener != null && mIsNeedNext) {
                    mOnNextActionListener.onNextAction();
                }
            }
        });
        mTrialVipPopup.show(mActivity.get().getSupportFragmentManager(), TrialVipPopup.TAG);
        UserConfig userConfig = App.getUserConfig();
        userConfig.setTrialLastTime(System.currentTimeMillis());
        userConfig.saveConfig();
    }

    @Override
    public boolean isApplicable() {
        UserConfig userConfig = App.getUserConfig();
        Profile profile = App.get().getProfile();
        Options options = App.get().getOptions();
        if (DateUtils.isDayBeforeToday(userConfig.getTrialLastTime())) {
            userConfig.setTrialVipPopupCounter(UserConfig.DEFAULT_SHOW_COUNT);
            userConfig.saveConfig();
        }
        return !profile.paid && !profile.premium &&
                userConfig.getTrialVipCounter() < options.getMaxShowCountTrialVipPopup() &&
                options.trialVipExperiment.enabled && new GoogleMarketApiManager().isMarketApiAvailable();
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
            final Fragment fragment = f == null ?
                    TransparentMarketFragment.newInstance(App.from(mActivity.get()).getOptions().trialVipExperiment.subscriptionSku, true) : f;
            fragment.setRetainInstance(true);
            if (fragment instanceof ITransparentMarketFragmentRunner) {
                ((ITransparentMarketFragmentRunner) fragment).setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseActions() {
                    @Override
                    public void onPurchaseSuccess() {
                        mIsNeedNext = false;
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
            }
            if (!fragment.isAdded()) {
                addTransparentMarketFragment(fragment);
            } else {
                removeTransparentMarketFragment(fragment);
                addTransparentMarketFragment(fragment);
            }
        }
    }

    private void addTransparentMarketFragment(Fragment fragment) {
        if (isFragmentApplicable()) {
            mActivity.get().getSupportFragmentManager().beginTransaction()
                    .add(fragment, TransparentMarketFragment.class.getSimpleName()).commit();
        }
    }

    private void removeTransparentMarketFragment(Fragment fragment) {
        if (isFragmentApplicable()) {
            mActivity.get().getSupportFragmentManager().
                    beginTransaction().remove(fragment).commit();
        }
    }

    private boolean isFragmentApplicable() {
        return mActivity != null && mActivity.get() != null && mActivity.get().isActivityRestoredState();
    }
}
