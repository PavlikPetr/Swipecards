package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.LayoutExperiment41Binding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
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

    val userAvatar: ObservableField<String> = ObservableField()
    val randomLeftPhoto: ObservableField<Int> = ObservableField()
    val randomRightPhoto: ObservableField<Int> = ObservableField()
    var profileSubscription: Subscription
    @Inject lateinit var state: TopfaceAppState

    init {
        setUrlAvatar(App.get().profile)
        App.get().inject(this)
        profileSubscription = state.getObservable(Profile::class.java).subscribe {
            setUrlAvatar(it)
            setRandomPhoto(it)
        }
    }

    fun setRandomPhoto(profile: Profile) =
            with(Utils.randomImageRes(PHOTO_COUNT,
                    if (profile.sex == Profile.BOY) getFakeAvatars(profile) else getFakeAvatars(profile))) {
                randomLeftPhoto.set(this[0])
                randomRightPhoto.set(this[1])
            }

    fun getFakeAvatars(profile: Profile): List<Int> {
        val imgs = App.getContext().resources.obtainTypedArray(
                if (profile.sex === Profile.GIRL) R.array.fake_boys_without_blur else R.array.fake_girls_without_blur)
        return with(arrayListOf<Int>()) {
            (0..imgs.length() - 1).forEach {
                this@with.add(imgs.getResourceId(it,
                        if (profile.dating.sex === Profile.GIRL) R.drawable.girl_1 else R.drawable.man_1))
            }
            this@with
        }
    }

    fun setFakeAvatar(profile: Profile) = if (profile.sex == Profile.BOY)
        R.drawable.upload_photo_male
    else
        R.drawable.upload_photo_female


    fun setUrlAvatar(profile: Profile) {
        val photoUrl = if (profile.photo != null) profile.photo.defaultLink else Utils.EMPTY
        userAvatar.set(if (TextUtils.isEmpty(photoUrl)) Utils.getLocalResUrl(setFakeAvatar(profile)) else photoUrl)
    }

    override fun release() {
        super.release()
        profileSubscription.safeUnsubscribe()
    }

}
