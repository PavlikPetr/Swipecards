package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.support.annotation.IntDef

/**
 * Подтип текущего эксперимента
 */
object ExperimentSubType {
    const val EXPERIMENT_SUBTYPE = "experiment_sub_type"

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(SubTypeNone, SubType4_1, SubType4_2, SubType4_3)
    annotation class ExperimentsSubType

    const val SubTypeNone = 0L
    const val SubType4_1 = 1L
    const val SubType4_2 = 2L
    const val SubType4_3 = 3L
}