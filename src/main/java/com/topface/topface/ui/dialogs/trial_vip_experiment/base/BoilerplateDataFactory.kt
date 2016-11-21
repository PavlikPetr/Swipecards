package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_2
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_3

/**
 * Фабрика данных для инфы на шаблонах разных экспериментов
 * Created by tiberal on 16.11.16.
 */
class BoilerplateDataFactory : IBoilerplateFactory<BoilerplateData> {

    override fun construct(@ExperimentsType.ExperimentsType type: Long, @ExperimentsType.ExperimentsType subType: Long): BoilerplateData =
            when (type) {
                ExperimentsType.EXPERIMENT_0 -> BoilerplateData.create {
                    buttonText = R.string.trial_vip_button_text
                    title = 0
                    description = 0
                }
                ExperimentsType.EXPERIMENT_1, EXPERIMENT_2, EXPERIMENT_3 -> BoilerplateData.create {
                    title = R.string.free_vip
                }
                ExperimentsType.EXPERIMENT_4 -> subTypeChooser(subType)

                ExperimentsType.EXPERIMENT_5 -> BoilerplateData.create {
                    title = R.string.free_vip
                }
                else -> BoilerplateData.create {
                    title = R.string.free_vip
                }

            }

    fun subTypeChooser(subType: Long) =
            when (subType) {
                ExperimentsType.SubType4_1-> BoilerplateData.create {
                }
                ExperimentsType.SubType4_2 -> BoilerplateData.create {
                    title = if (App.get().profile.sex == Profile.BOY) R.string.write_any_girl else R.string.write_any_boy
                }
                ExperimentsType.SubType4_3 -> BoilerplateData.create {
                    title = R.string.know_your_guests
                }
                else -> BoilerplateData.create {
                }
            }

}