package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.topface.topface.R

/**
 * Фабрика метрик, для разных типов попопов триал эксперимента
 * Created by tiberal on  16.11.16.
 */
class MetricsFactory(private val mContext: Context, val parent: ViewGroup,
                     val args: Bundle) : IBoilerplateFactory<BoilerplateDialogMetrics> {


    override fun construct(@ExperimentsType.ExperimentsType type: Long) =
            when (type) {
                ExperimentsType.EXPERIMENT_0 -> BoilerplateDialogMetrics.create {
                    popupBackground = R.drawable.trial_vip_background
                    getVipButtonBackground = R.drawable.btn_blue_selector
                    contentBottomMargin = R.dimen.toolbar_title_padding_left
                    initStandardMetrics(this)
                }
                ExperimentsType.EXPERIMENT_1, ExperimentsType.EXPERIMENT_3 -> BoilerplateDialogMetrics.create {
                    initStandardMetrics(this)
                }
                ExperimentsType.EXPERIMENT_2 -> BoilerplateDialogMetrics.create {
                    isSpecialOffer = true
                    initStandardMetrics(this)
                }
                ExperimentsType.EXPERIMENT_4 -> BoilerplateDialogMetrics.create {
                    titleTopMargin = R.dimen.experiment_5_title_top_margin
                    titleBottomMargin = R.dimen.experiment_5_title_bottom_margin
                    contentBottomMargin = R.dimen.experiment_5_content_bottom_margin
                    getVipBottomMargin = R.dimen.experiment_5_get_vip_bottom_margin
                    descriptionBottomMargin = R.dimen.experiment_5_description_bottom_margin
                }
                ExperimentsType.EXPERIMENT_5, ExperimentsType.EXPERIMENT_6 -> BoilerplateDialogMetrics.create {
                    titleTopMargin = R.dimen.experiment_5_title_top_margin
                    titleBottomMargin = R.dimen.experiment_5_title_bottom_margin
                    contentBottomMargin = R.dimen.experiment_5_content_bottom_margin
                    getVipBottomMargin = R.dimen.experiment_5_get_vip_bottom_margin
                    descriptionBottomMargin = R.dimen.experiment_5_description_bottom_margin
                    popupBackground = R.drawable.trial_vip_popup_bg
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