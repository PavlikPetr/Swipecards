package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.buy.TransparentMarketFragment;
import com.topface.topface.ui.views.ITransparentMarketFragmentRunner;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.popups.PopupManager;

import java.lang.ref.WeakReference;


public class TrialVipPopupAction implements IStartAction {

    private String mFrom;
    private int mPriority;
    private WeakReference<BaseFragmentActivity> mActivity;
    private OnNextActionListener mOnNextActionListener;
    private boolean mIsNeedNext = true;

    public TrialVipPopupAction(BaseFragmentActivity activity, int priority, String from) {
        mActivity = new WeakReference<>(activity);
        mPriority = priority;
        mFrom = from;
    }

    @Override
    public void callInBackground() {
    }

    @Override
    public void callOnUi() {
        if (mActivity == null || mActivity.get() == null) {
            return;
        }

        final TrialVipPopup popup = TrialVipPopup.newInstance(true);
        popup.setOnSubscribe(new TrialVipPopup.OnFragmentActionsListener() {
            @Override
            public void onSubscribeClick() {
                Fragment f = popup.getActivity().getSupportFragmentManager().findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
                final Fragment fragment = f == null ?
                        TransparentMarketFragment.newInstance(App.get().getOptions().trialVipExperiment.subscriptionSku, true, TrialVipPopup.TAG) : f;
                fragment.setRetainInstance(true);
                if (fragment instanceof ITransparentMarketFragmentRunner) {
                    ((ITransparentMarketFragmentRunner) fragment).setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseActions() {
                        @Override
                        public void onPurchaseSuccess() {
                            popup.dismiss();
                        }

                        @Override
                        public void onPopupClosed() {
                        }
                    });
                    FragmentTransaction transaction = popup.getActivity().getSupportFragmentManager().beginTransaction();
                    if (!fragment.isAdded()) {
                        transaction.add(R.id.fragment_content, fragment, TransparentMarketFragment.class.getSimpleName()).commit();
                    } else {
                        transaction.remove(fragment)
                                .add(R.id.fragment_content, fragment, TransparentMarketFragment.class.getSimpleName()).commit();
                    }
                }
            }

            @Override
            public void onFragmentFinish() {
                PopupManager.INSTANCE.informManager(mFrom);
            }
        });
        if (popup != null) {
            popup.show(mActivity.get().getSupportFragmentManager(), TrialVipPopup.TAG);
        }
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

    private void showSubscriptionPopup() {
        if (mActivity != null && mActivity.get() != null) {
            Fragment f = mActivity.get().getSupportFragmentManager().findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
            final Fragment fragment = f == null ?
                    TransparentMarketFragment.newInstance(App.get().getOptions().trialVipExperiment.subscriptionSku, true, "TrialVipPopup") : f;
            if (fragment instanceof ITransparentMarketFragmentRunner) {
                ((ITransparentMarketFragmentRunner) fragment).setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseActions() {
                    @Override
                    public void onPurchaseSuccess() {
                        if (null != mTrialVipPopup) {
                            mTrialVipPopup.dismiss();
                        }
                    }

                    @Override
                    public void onPopupClosed() {
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
