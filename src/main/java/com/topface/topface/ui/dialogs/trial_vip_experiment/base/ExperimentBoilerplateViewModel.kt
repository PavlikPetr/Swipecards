package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.ui.dialogs.trial_vip_experiment.TransparentMarketFragmentRunner
import org.jetbrains.anko.dimen

/**
 * VM для шаблона попапов экспериментов.
 * Created by tiberal on 15.11.16.
 */
class ExperimentBoilerplateViewModel(private val mPopupRunner: TransparentMarketFragmentRunner.IRunner,
                                     dialogMetrics: BoilerplateDialogMetrics, dialogData: BoilerplateData) {

    private companion object {
        const val EMPTY = ""
    }

    private val mContext = App.getContext()

    val titleTopMargin = ObservableInt(mContext.dimen(dialogMetrics.titleTopMargin))
    val titleBottomMargin = ObservableInt(mContext.dimen(dialogMetrics.titleBottomMargin))
    val contentBottomMargin = ObservableInt(mContext.dimen(dialogMetrics.contentBottomMargin))
    val getVipBottomMargin = ObservableInt(mContext.dimen(dialogMetrics.getVipBottomMargin))
    val descriptionBottomMargin = ObservableInt(mContext.dimen(dialogMetrics.descriptionBottomMargin))
    val popupBackground = ObservableInt(dialogMetrics.popupBackground)
    val getVipButtonBackground = ObservableInt(dialogMetrics.getVipButtonBackground)

    val title = ObservableField<String>(mContext.getString(dialogData.title) ?: EMPTY)
    val buttonText = ObservableField<String>(mContext.getString(dialogData.buttonText) ?: EMPTY)
    val description = ObservableField<String>(mContext.getString(dialogData.description) ?: EMPTY)

    fun getVip() = mPopupRunner.runMarketPopup()

}