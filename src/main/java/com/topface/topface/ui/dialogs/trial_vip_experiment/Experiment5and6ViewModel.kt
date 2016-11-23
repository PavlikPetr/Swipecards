package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.LayoutExperiment41Binding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getDrawableListFromArrayId
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject


/**
 * модель для эксперимента 5/6
 * Created by ppavlik on 23.11.16.
 */
class Experiment5and6ViewModel() {

    val sendForFree: ObservableField<String> = ObservableField()
    var profileSubscription: Subscription
    @Inject lateinit var state: TopfaceAppState

    init {
        App.get().inject(this)
        setDependsOnSexText(App.get().profile)
        profileSubscription = state.getObservable(Profile::class.java).subscribe {
            setDependsOnSexText(it)
        }
    }

    private fun setDependsOnSexText(profile: Profile) =
            sendForFree.set(
                    if (profile.sex == Profile.BOY)
                        R.string.buy_vip_write_anyone.getString()
                    else
                        R.string.buy_vip_write_anyone_boy.getString())
}
