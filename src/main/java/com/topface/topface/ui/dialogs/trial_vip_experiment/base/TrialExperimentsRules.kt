package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.os.Bundle
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_3
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_4
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.SUBTYPE_4_1
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.SUBTYPE_4_2
import com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3.Experiment1_2_3_Adapter.Companion.MESSAGE_FIRST
import com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3.Experiment1_2_3_Adapter.Companion.VIEWS_FIRST
import com.topface.topface.ui.dialogs.trial_vip_experiment.getBundle
import com.topface.topface.ui.fragments.dating.DatingButtonsViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.profile.UserProfileFragment
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.config.UserConfig
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit

/**
 * Правила показа попапов триала
 * Created by tiberal on 18.11.16.
 */
object TrialExperimentsRules {

    const val PROFILE_INTERVAL = 3L
    const val SYMPATHY_COUNT = 10L
    const val SHOWS_IN_USER_PROFILE = 1

    private val mConfig: UserConfig = App.getUserConfig()
    private var mShowInUserProfileSubscription: Subscription? = null

    fun Any.tryShowTrialPopup(type: Long = App.get().options.trialVipExperiment.androidTrialPopupExp, navigator: IFeedNavigator) {
        when (type) {
            EXPERIMENT_3, EXPERIMENT_4 -> {
                when (this@tryShowTrialPopup) {
                    is DatingButtonsViewModel -> showTrialAfterSympathy(type, navigator)
                    is UserProfileFragment -> showTrialFromUserProfile(type, navigator)
                    else -> return
                }
            }
            else -> Unit
        }
    }


    private fun showTrialAfterSympathy(type: Long, navigator: IFeedNavigator) {
        val count = mConfig.getSympathyCount<Int>()
        Debug.log("FUCKING_EXP count symp = $count")
        if (count.configFieldInfo.amount == SYMPATHY_COUNT) {
            startTrialPopup(type, navigator, type.getBundle(VIEWS_FIRST, SUBTYPE_4_2))
        }
        mConfig.setSympathyCount()
    }

    private fun showTrialFromUserProfile(type: Long, navigator: IFeedNavigator) {
        val count = mConfig.getShowsInUserProfile<Int>()
        Debug.log("FUCKING_EXP trial_shows_count = $count")
        if (count.configFieldInfo.amount <= SHOWS_IN_USER_PROFILE) {
            mShowInUserProfileSubscription = Observable.interval(0, PROFILE_INTERVAL, TimeUnit.SECONDS)
                    .skip(1)
                    .applySchedulers()
                    .subscribe(object : RxUtils.ShortSubscription<Long>() {
                        override fun onNext(time: Long?) {
                            startTrialPopup(type, navigator, type.getBundle(MESSAGE_FIRST, SUBTYPE_4_1))
                            unsubscribe()
                        }
                    })
        }
        mConfig.setShowsInUserProfile()
    }

    private fun startTrialPopup(type: Long, navigator: IFeedNavigator, args: Bundle = Bundle()) {
        Debug.log("FUCKING_EXP popup started")
        navigator.showTrialPopup(type, args)
    }

    fun release() {
        mShowInUserProfileSubscription.safeUnsubscribe()
    }
}