package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import com.topface.topface.R

/**
 * Фабрика метрик, для разных типов попопов триал эксперимента
 * Created by tiberal on  16.11.16.
 */
class MetricsFactory : IBoilerplateFactory<BoilerplateDialogMetrics> {


    override fun construct(@ExperimentsType.ExperimentsType type: Long) =
            when (type) {
                ExperimentsType.EXPERIMENT_1, ExperimentsType.EXPERIMENT_3 -> BoilerplateDialogMetrics.create {
                    initStandardMetrics(this)
                }
                ExperimentsType.EXPERIMENT_2 -> BoilerplateDialogMetrics.create {
                    isSpecialOffer = true
                    initStandardMetrics(this)
                }
                ExperimentsType.EXPERIMENT_4 -> BoilerplateDialogMetrics.create {
                }
                ExperimentsType.EXPERIMENT_5 -> BoilerplateDialogMetrics.create {
                }
                else -> BoilerplateDialogMetrics.create {
                }
            }


    private fun initStandardMetrics(builder: BoilerplateDialogMetrics.Builder): BoilerplateDialogMetrics.Builder {
        builder.titleTopMargin = R.dimen.experiment_1_2_3_title_top_margin
        builder.titleBottomMargin = R.dimen.experiment_1_2_3_title_bottom_margin
        builder.contentBottomMargin = R.dimen.experiment_1_2_3_content_bottom_margin
        builder.getVipBottomMargin = R.dimen.experiment_1_2_3_get_vip_bottom_margin
        builder.descriptionBottomMargin = R.dimen.experiment_1_2_3_description_bottom_margin
        return builder
    }

}