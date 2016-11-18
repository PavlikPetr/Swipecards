package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.LayoutExperiment41Binding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.Utils
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject

/**
 * модель для эксперимента 4_1
 * Created by siberia87 on 15.11.16.
 */
class Experiment41ViewModel(binding: LayoutExperiment41Binding) :
        BaseViewModel<LayoutExperiment41Binding>(binding) {

    val userAvatar: ObservableField<String> = ObservableField(Utils.getLocalResUrl(R.drawable.upload_photo_female))
    var profileSubscription: Subscription
    @Inject lateinit var state: TopfaceAppState

    init {
        setUrlAvatar(App.get().profile)
        App.get().inject(this)
        profileSubscription = state.getObservable(Profile::class.java).subscribe { profile -> setUrlAvatar(profile) }
    }

    fun setFakeAvatar(profile: Profile) = when {
        (profile.sex == Profile.BOY) -> R.drawable.upload_photo_male
        else -> R.drawable.upload_photo_female
    }


    fun setUrlAvatar(profile: Profile) {
        val photoUrl = if (profile.photo != null) profile.photo.defaultLink else Utils.EMPTY
        userAvatar.set(if (TextUtils.isEmpty(photoUrl)) Utils.getLocalResUrl(setFakeAvatar(profile)) else photoUrl)
    }

    override fun release() {
        super.release()
        profileSubscription.unsubscribe()
    }
}
