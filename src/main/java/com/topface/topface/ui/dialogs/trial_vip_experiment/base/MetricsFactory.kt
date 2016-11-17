package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import com.topface.topface.R

/**
 * Фабрика метрик, для разных типов попопов триал эксперимента
 * Created by tiberal on  16.11.16.
 */
class MetricsFactory : IBoilerplateFactory<BoilerplateDialogMetrics> {


    override fun construct(@ExperimentsType.ExperimentsType type: Long) =
            when (type) {
                ExperimentsType.EXPERIMENT_1 -> BoilerplateDialogMetrics.create {
                    titleTopMargin = R.dimen.experiment_1_2_3_title_top_margin
                    titleBottomMargin = R.dimen.experiment_1_2_3_title_bottom_margin
                    contentBottomMargin = R.dimen.experiment_1_2_3_content_bottom_margin
                    getVipBottomMargin = R.dimen.experiment_1_2_3_get_vip_bottom_margin
                    descriptionBottomMargin = R.dimen.experiment_1_2_3_description_bottom_margin
                }
                ExperimentsType.EXPERIMENT_2 -> BoilerplateDialogMetrics.create {
                }
                ExperimentsType.EXPERIMENT_3 -> BoilerplateDialogMetrics.create {
                }
                ExperimentsType.EXPERIMENT_4 -> BoilerplateDialogMetrics.create {
                }
                ExperimentsType.EXPERIMENT_5 -> BoilerplateDialogMetrics.create {
                }
                else -> BoilerplateDialogMetrics.create {
                }
            }

}