package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment4Binding
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription

/**
 * Базовая ВьюМодел для экспериментов4 (2-3)
 */
abstract class Experiment4BaseViewModel(binding: Experiment4Binding) : BaseViewModel<Experiment4Binding>(binding) {


    companion object {
        // продолжительность задержки для анимации фэйковых аватаров
        const val FIRST: Long = 0L
        const val SECOND: Long = 100L
        const val THIRD: Long = 200L
        const val FOURTH: Long = 300L
        const val FIFTH: Long = 400L
    }

    val profileSubscription: Subscription

    val avatar: ObservableField<String> = ObservableField()
    abstract val popupMessage: ObservableField<String>
    abstract val fakeAvatars: List<Int>
    abstract val imageUnderAvatar: ObservableField<Int>
    abstract val imageLeftTop: ObservableField<Int>
    abstract val imageRightBottom: ObservableField<Int>

    init {
        setUrlAvatar(App.get().profile)
        setAvatarsForFakes()
        profileSubscription = App.getAppComponent().appState().getObservable(Profile::class.java)
                .subscribe { setUrlAvatar(it) }
    }

    fun setFakeAvatar(profile: Profile) = if (profile.sex == Profile.BOY) R.drawable.upload_photo_male else R.drawable.upload_photo_female

    fun setUrlAvatar(profile: Profile) {
        val photoUrl = if (profile.photo != null) profile.photo.defaultLink else Utils.EMPTY
        avatar.set(if (TextUtils.isEmpty(photoUrl)) Utils.getLocalResUrl(setFakeAvatar(profile)) else photoUrl)
    }

    fun setAvatarsForFakes() {
        val avat = fakeAvatars
        binding.firstFakeAvatar.setImageResource(avat[0])
        binding.secondFakeAvatar.setImageResource(avat[1])
        binding.thirdFakeAvatar.setImageResource(avat[2])
        binding.fourthFakeAvatar.setImageResource(avat[3])
        binding.fifthFakeAvatar.setImageResource(avat[4])
    }

    override fun release() {
        profileSubscription.safeUnsubscribe()
        super.release()
    }

}