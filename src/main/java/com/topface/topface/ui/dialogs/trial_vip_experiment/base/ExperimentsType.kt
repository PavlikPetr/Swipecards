package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.support.annotation.IntDef

/**
 * Тип текущего экспиремента
 * Created by tiberal on 16.11.16.
 */

object ExperimentsType {

    const val EXPERIMENT_TYPE = "experiment_type"
    const val EXPERIMENT_SUBTYPE = "experiment_sub_type"

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(EXPERIMENT_0, EXPERIMENT_1, EXPERIMENT_2, EXPERIMENT_3, EXPERIMENT_4, EXPERIMENT_5, EXPERIMENT_6)
    annotation class ExperimentsType

    const val EXPERIMENT_0 = 0L
    const val EXPERIMENT_1 = 1L
    const val EXPERIMENT_2 = 2L
    const val EXPERIMENT_3 = 3L
    const val EXPERIMENT_4 = 4L
    const val EXPERIMENT_5 = 5L
    const val EXPERIMENT_6 = 6L


    @IntDef(SUBTYPE_NONE, SUBTYPE_4_1, SUBTYPE_4_2, SUBTYPE_4_3)
    annotation class ExperimentsSubType

    const val SUBTYPE_NONE = 0L
    const val SUBTYPE_4_1 = 41L
    const val SUBTYPE_4_2 = 42L
    const val SUBTYPE_4_3 = 43L

}
