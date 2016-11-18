package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.support.annotation.IntDef

/**
 * Тип текущего экспиремента
 * Created by tiberal on 16.11.16.
 */

object ExperimentsType {

    const val EXPERIMENT_TYPE = "experiment_type"

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(EXPERIMENT_1, EXPERIMENT_2, EXPERIMENT_3, EXPERIMENT_4_1, EXPERIMENT_4_2, EXPERIMENT_4_3, EXPERIMENT_5, EXPERIMENT_6)
    annotation class ExperimentsType

    const val EXPERIMENT_1 = 1L
    const val EXPERIMENT_2 = 2L
    const val EXPERIMENT_3 = 3L
    const val EXPERIMENT_4_1 = 41L
    const val EXPERIMENT_4_2 = 42L
    const val EXPERIMENT_4_3 = 43L
    const val EXPERIMENT_5 = 5L
    const val EXPERIMENT_6 = 6L

}
