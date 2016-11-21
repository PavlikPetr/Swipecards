package com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3

import com.topface.topface.App

/**
 * VM для экспирементов 1-2-3
 * Created by tiberal on 16.11.16.
 */
class Experiment1_2_3_ViewModel(val type: Int = 0) {

    val adapter = Experiment1_2_3_Adapter(App.getContext(), type)

}