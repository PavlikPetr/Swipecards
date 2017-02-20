package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.ui.dialogs.trial_vip_experiment.IRunner
import com.topface.topface.ui.dialogs.trial_vip_experiment.TransparentMarketFragmentRunner
import com.topface.topface.ui.dialogs.trial_vip_experiment.TrialVipExperimentStatistics
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString

/**
 * VM для шаблона попапов экспериментов.
 * Created by tiberal on 15.11.16.
 */
class ExperimentBoilerplateViewModel(private val mPopupRunner: IRunner,
                                     dialogMetrics: BoilerplateDialogMetrics, dialogData: BoilerplateData) {

    val titleTopMargin = ObservableInt(dialogMetrics.titleTopMargin.getDimen().toInt())
    val titleBottomMargin = ObservableInt(dialogMetrics.titleBottomMargin.getDimen().toInt())
    val contentBottomMargin = ObservableInt(dialogMetrics.contentBottomMargin.getDimen().toInt())
    val getVipBottomMargin = ObservableInt(dialogMetrics.getVipBottomMargin.getDimen().toInt())
    val getVipTopMargin = ObservableInt(dialogMetrics.getVipTopMargin.getDimen().toInt())
    val descriptionBottomMargin = ObservableInt(dialogMetrics.descriptionBottomMargin.getDimen().toInt())
    val popupBackground = ObservableInt(dialogMetrics.popupBackground)
    val getVipButtonBackground = ObservableInt(dialogMetrics.getVipButtonBackground)
    val specialOfferVisibility = ObservableInt(specialOfferVisibility(dialogMetrics.isSpecialOffer))

    val title = ObservableField<String>(dialogData.title.getString())
    val buttonText = ObservableField<String>(dialogData.buttonText.getString())
    val description = ObservableField<String>(dialogData.description.getString())

    private fun specialOfferVisibility(visibility: Boolean) = if (visibility) {
        View.VISIBLE
    } else {
        View.GONE
    }

    fun getVip() {
        TrialVipExperimentStatistics.sendPurchaseButtonPressed()
        mPopupRunner.runMarketPopup()
    }
}