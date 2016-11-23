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

    private val boysName = arrayListOf(R.string.fake_male_name_1, R.string.fake_male_name_2, R.string.fake_male_name_3, R.string.fake_male_name_4, R.string.fake_male_name_5, R.string.fake_male_name_6, R.string.fake_male_name_7, R.string.fake_male_name_8, R.string.fake_male_name_9, R.string.fake_male_name_10)

    private val girlsName = arrayListOf(R.string.fake_female_name_1, R.string.fake_female_name_2, R.string.fake_female_name_3, R.string.fake_female_name_4, R.string.fake_female_name_5, R.string.fake_female_name_6, R.string.fake_female_name_7, R.string.fake_female_name_8, R.string.fake_female_name_9, R.string.fake_female_name_10)

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
            setRandomPhoto(it)

            with(Utils.randomImageRes(2, if (App.get().profile.sex == Profile.BOY) girlsName else boysName)) {
                vipBannerText.set(String.format(context.resources.getString(R.string.description_experiment4_view1), context.resources.getString(this[0]), context.resources.getString(this[1])))
            }

        }

    }

    private fun setRandomPhoto(profile: Profile) =
            with(Utils.randomImageRes(PHOTO_COUNT,
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
