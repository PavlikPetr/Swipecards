package com.topface.topface.utils.controllers.startactions;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.trial_vip_experiment.IOnFragmentFinishDelegate;
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentBoilerplateFragment;
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.popups.PopupManager;

import java.lang.ref.WeakReference;


public class TrialVipPopupAction implements IStartAction, IOnFragmentFinishDelegate {

    private String mFrom;
    private int mPriority;
    private WeakReference<BaseFragmentActivity> mActivity;

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
        if (mActivity != null && mActivity.get() != null) {
            chooseShowTrialVipPopup();
        }
    }

    private void chooseShowTrialVipPopup() {
        ExperimentBoilerplateFragment popup = ExperimentBoilerplateFragment
                .newInstance(getTrialVipType(), true);
        popup.setOnFragmentFinishDelegate(this);
        popup.show(mActivity.get().getSupportFragmentManager(), ExperimentBoilerplateFragment.TAG);
        UserConfig userConfig = App.getUserConfig();
        userConfig.setTrialLastTime(System.currentTimeMillis());
        userConfig.saveConfig();
    }

    /**
     * Get customized trial vip popup type
     *
     * @return experiment number
     */
    public static long getTrialVipType() {
        long typeFromServer = App.get().getOptions().trialVipExperiment.androidTrialPopupExp;
        // если сервер прислал 4-й эксперимент, то для очереди и после выхода с экрана покупок показать вью из 1-го
        if (typeFromServer == 4 || typeFromServer == 3) {
            return 1;
        }
        return typeFromServer;
    }

    @Override
    public boolean isApplicable() {
        UserConfig userConfig = App.getUserConfig();
        Profile profile = App.get().getProfile();
        Options options = App.get().getOptions();
        if (DateUtils.isDayBeforeToday(userConfig.getTrialLastTime())) {
            userConfig.setQueueTrialVipPopupCounter(UserConfig.DEFAULT_SHOW_COUNT);
            userConfig.saveConfig();
        }
        return !profile.paid && !profile.premium &&
                userConfig.getQueueTrialVipCounter() < options.getMaxShowCountTrialVipPopup() &&
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
    public void closeFragmentByForm() {
        PopupManager.INSTANCE.informManager(mFrom);
    }
}
