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
import java.util.*
import javax.inject.Inject

/**
 * модель для эксперимента 4_1
 * Created by siberia87 on 15.11.16.
 */
class Experiment41ViewModel(binding: LayoutExperiment41Binding) :
        BaseViewModel<LayoutExperiment41Binding>(binding) {

    companion object {
        const val PHOTO_COUNT = 2
    }

    val girls = listOf(R.drawable.girl_1, R.drawable.girl_2, R.drawable.girl_3, R.drawable.girl_4, R.drawable.girl_5, R.drawable.girl_6, R.drawable.girl_7, R.drawable.girl_8, R.drawable.girl_9, R.drawable.girl_10)

    val boys = listOf(R.drawable.man_1, R.drawable.man_2, R.drawable.man_3, R.drawable.man_4, R.drawable.man_5, R.drawable.man_6, R.drawable.man_7, R.drawable.man_8, R.drawable.man_9, R.drawable.man_10)

    val userAvatar: ObservableField<String> = ObservableField()
    val randomLeftPhoto: ObservableField<Int> = ObservableField()
    val randomRightPhoto: ObservableField<Int> = ObservableField()
    var profileSubscription: Subscription
    @Inject lateinit var state: TopfaceAppState

    init {
        setUrlAvatar(App.get().profile)
        App.get().inject(this)
        profileSubscription = state.getObservable(Profile::class.java).subscribe { profile ->
            setUrlAvatar(profile)
            setRandomPhoto(profile)
        }
    }

    fun setRandomPhoto(profile: Profile) {
        with(Utils.randomImageRes(PHOTO_COUNT, if (profile.sex == Profile.BOY) girls else boys)) {
            randomLeftPhoto.set(this[0])
            randomRightPhoto.set(this[1])
        }
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
