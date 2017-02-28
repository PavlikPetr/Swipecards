package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.os.Bundle
import com.topface.topface.R

/**
 * Фабрика метрик, для разных типов попопов триал эксперимента
 * Created by tiberal on  16.11.16.
 */
class MetricsFactory(val args: Bundle) {

    fun createBoilerplateDialogMetrics() =
            BoilerplateDialogMetrics.create {
                popupBackground = R.drawable.trial_vip_background
                getVipButtonBackground = R.drawable.btn_blue_selector
                contentBottomMargin = R.dimen.toolbar_title_padding_left
                titleTopMargin = R.dimen.experiment_1_2_3_title_top_margin
                titleBottomMargin = R.dimen.experiment_1_2_3_title_bottom_margin
                contentBottomMargin = R.dimen.experiment_1_2_3_content_bottom_margin
                getVipBottomMargin = R.dimen.experiment_1_2_3_get_vip_bottom_margin
                descriptionBottomMargin = R.dimen.experiment_1_2_3_description_bottom_margin
            }
}