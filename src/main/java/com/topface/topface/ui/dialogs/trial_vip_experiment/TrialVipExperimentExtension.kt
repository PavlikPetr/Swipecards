package com.topface.topface.ui.dialogs.trial_vip_experiment

import android.os.Bundle
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType
import com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3.Experiment1_2_3_Adapter

/**
 * Created by ppavlik on 23.11.16.
 */


fun Long.getBundle(mode: Int = 0, @ExperimentsType.ExperimentsSubType subType: Long = ExperimentsType.SUBTYPE_NONE): Bundle =
        when (this) {
            ExperimentsType.EXPERIMENT_3 -> Bundle().apply {
                this.putInt(Experiment1_2_3_Adapter.MODE, mode)
            }
            ExperimentsType.EXPERIMENT_4 -> Bundle().apply {
                this.putLong(ExperimentsType.EXPERIMENT_SUBTYPE, subType)
            }
            else -> Bundle()
        }
