package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.databinding.LayoutExperiment41Binding
import com.topface.topface.viewModels.BaseViewModel

/**
 * модель для эксперимента 4_1
 * Created by siberia87 on 15.11.16.
 */
class Experiment41ViewModel(binding: LayoutExperiment41Binding) :
        BaseViewModel<LayoutExperiment41Binding>(binding) {
    val userAvatar = ObservableField(App.get().profile.photo.defaultLink)
}
