package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.utils.extensions.getString


/**
 * модель для эксперимента 5/6
 * Created by ppavlik on 23.11.16.
 */
class Experiment5and6ViewModel() {
    val sendForFree: ObservableField<String> = ObservableField(if (App.get().profile.sex == Profile.BOY)
        R.string.buy_vip_write_anyone.getString()
    else
        R.string.buy_vip_write_anyone_boy.getString())
}
