package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.utils.extensions.getString

/**
 * Фабрика данных для инфы на шаблонах разных экспериментов
 * Created by tiberal on 16.11.16.
 */
class BoilerplateDataFactory : IBoilerplateFactory<BoilerplateData> {

    override fun construct(@ExperimentsType.ExperimentsType type: Long) =
            when (type) {
                ExperimentsType.EXPERIMENT_1 -> BoilerplateData.create {
                    title = R.string.free_vip
                }
                ExperimentsType.EXPERIMENT_2 -> BoilerplateData.create {
                }
                ExperimentsType.EXPERIMENT_3 -> BoilerplateData.create {
                }
                ExperimentsType.EXPERIMENT_4_2 -> BoilerplateData.create {
                    title = if (App.get().profile.sex == Profile.BOY) R.string.write_any_girl else R.string.write_any_boy
                }
                ExperimentsType.EXPERIMENT_4_3 -> BoilerplateData.create {
                    title = R.string.know_your_guests
                }
                ExperimentsType.EXPERIMENT_5 -> BoilerplateData.create {
                    title = R.string.free_vip
                }
                ExperimentsType.EXPERIMENT_6 -> BoilerplateData.create {
                    title = R.string.free_vip
                }
                else -> BoilerplateData.create {
                }
            }
}