package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.LayoutExperiment41Binding
import com.topface.topface.viewModels.BaseViewModel
import org.jetbrains.anko.imageResource

/**
 * модель для эксперимента 4_1
 * Created by siberia87 on 15.11.16.
 */
class Experiment41ViewModel(binding: LayoutExperiment41Binding) :
        BaseViewModel<LayoutExperiment41Binding>(binding) {
    val userAvatar = ObservableField(App.get().profile.photo.defaultLink ?: setFakeAvatar())

    fun setFakeAvatar() {
        binding.userAvatar.imageResource = when {
            (App.get().profile.sex == Profile.GIRL) ->  R.drawable.upload_photo_female
            else -> R.drawable.upload_photo_male
        }
    }
}
