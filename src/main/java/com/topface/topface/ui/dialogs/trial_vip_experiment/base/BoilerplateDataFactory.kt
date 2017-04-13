package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.os.Bundle
import android.view.ViewGroup
import com.topface.topface.R

/**
 * Фабрика данных для инфы на шаблонах разных экспериментов
 * Created by tiberal on 16.11.16.
 */
class BoilerplateDataFactory(val parent: ViewGroup, val args: Bundle) {

    fun createBoilerplateData() =
            BoilerplateData.create {
                buttonText = R.string.trial_vip_button_text
                title = 0
                description = 0
            }
}