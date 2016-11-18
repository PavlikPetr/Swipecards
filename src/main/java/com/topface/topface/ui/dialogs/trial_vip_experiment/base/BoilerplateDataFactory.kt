package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import com.topface.topface.R
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_2
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_3

/**
 * Фабрика данных для инфы на шаблонах разных экспериментов
 * Created by tiberal on 16.11.16.
 */
class BoilerplateDataFactory : IBoilerplateFactory<BoilerplateData> {

    override fun construct(@ExperimentsType.ExperimentsType type: Long) =
            when (type) {
                ExperimentsType.EXPERIMENT_1, EXPERIMENT_2, EXPERIMENT_3 -> BoilerplateData.create {
                    title = R.string.free_vip
                }
                ExperimentsType.EXPERIMENT_4 -> BoilerplateData.create {
                }
                ExperimentsType.EXPERIMENT_5 -> BoilerplateData.create {
                    title = R.string.free_vip
                }
                else -> BoilerplateData.create {
                    title = R.string.free_vip
                }
            }
}