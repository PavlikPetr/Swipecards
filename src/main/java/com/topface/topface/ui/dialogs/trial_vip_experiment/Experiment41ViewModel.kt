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
import java.util.*
import javax.inject.Inject


/**
 * модель для эксперимента 4_1
 * Created by siberia87 on 15.11.16.
 */
class Experiment41ViewModel(binding: LayoutExperiment41Binding) :
        BaseViewModel<LayoutExperiment41Binding>(binding) {

    private companion object {
        const val RANDOM_PHOTO_COUNT = 2
        const val RANDOM_NAME_COUNT = 2
    }

    val userAvatar: ObservableField<String> = ObservableField()
    val randomLeftPhoto: ObservableField<Int> = ObservableField()
    val randomRightPhoto: ObservableField<Int> = ObservableField()
    val vipBannerText: ObservableField<String> = ObservableField()
    var profileSubscription: Subscription
    @Inject lateinit var state: TopfaceAppState

    init {
        setUrlAvatar(App.get().profile)
        App.get().inject(this)
        profileSubscription = state.getObservable(Profile::class.java).subscribe {
            setUrlAvatar(it)
            setRandomUserAvatar(it)
            setRandomUserName(it)
        }
    }

    private fun setRandomUserName(profile: Profile) =
            with(Utils.chooseRandomResourceID(RANDOM_NAME_COUNT,
                    if (profile.sex == Profile.BOY)
                        R.array.fake_girls_name.getDrawableListFromArrayId(R.string.fake_female_name_1)
                    else
                        R.array.fake_girls_name.getDrawableListFromArrayId(R.string.fake_male_name_1))) {
                vipBannerText.set(String.format(R.string.description_experiment4_view1.getString(), this[0].getString(),
                        this[1].getString()))
            }

    private fun setRandomUserAvatar(profile: Profile) =
            with(Utils.chooseRandomResourceID(RANDOM_PHOTO_COUNT,
                    if (profile.sex == Profile.BOY)
                        R.array.fake_girls_without_blur.getDrawableListFromArrayId(R.drawable.girl_1)
                    else
                        R.array.fake_boys_without_blur.getDrawableListFromArrayId(R.drawable.man_1))) {
                randomLeftPhoto.set(this[0])
                randomRightPhoto.set(this[1])
            }

    private fun setFakeAvatar(profile: Profile) = Utils.getLocalResUrl(
            if (profile.sex == Profile.BOY)
                R.drawable.upload_photo_male
            else
                R.drawable.upload_photo_female)


    private fun setUrlAvatar(profile: Profile) {
        val photoUrl = if (profile.photo != null) profile.photo.defaultLink else Utils.EMPTY
        userAvatar.set(if (TextUtils.isEmpty(photoUrl)) {
            setFakeAvatar(profile)
        } else {
            photoUrl
        })
    }

    override fun release() {
        super.release()
        profileSubscription.safeUnsubscribe()
    }
}
