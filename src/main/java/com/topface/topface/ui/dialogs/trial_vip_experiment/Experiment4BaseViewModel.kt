package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import java.util.*
import javax.inject.Inject

/**
 * Базовая ВьюМодел для экспериментов4 (2-3)
 */
open class Experiment4BaseViewModel(binding: Experiment4Binding) : BaseViewModel<Experiment4Binding>(binding) {

    @Inject lateinit var state: TopfaceAppState

    var profileSubscription: Subscription
    var popupMessage = ObservableField<String>()
    var title = ObservableField<String>()
    var fakeAvatars = ObservableField<ArrayList<Int>>()
    val avatar: ObservableField<String> = ObservableField(Utils.getLocalResUrl(R.drawable.upload_photo_female))
    var imageUnderAvatar = ObservableField<Int>()
    var imageLeftTop = ObservableField<Int>()
    var imageRightBottom = ObservableField<Int>()

    // продолжительность щадержки для анимации фэйковых аватаров
    val first: Long = 0
    val second: Long = 100
    val third: Long = 200
    val fourth: Long = 300
    val fifth: Long = 400

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
        avatar.set(if (TextUtils.isEmpty(photoUrl)) Utils.getLocalResUrl(setFakeAvatar(profile)) else photoUrl)
    }

    override fun release() {
        profileSubscription.safeUnsubscribe()
        super.release()
    }
}