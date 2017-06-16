package com.topface.topface.experiments

import com.topface.topface.App
import com.topface.topface.data.experiments.AttractionExperimentGroup

/**
 * Здесь описана логика эксперимента с переключателем "только красивые" в фильтре знакомств
 */

fun Int.isSwitchPrettyControlVisible():Boolean = when(this) {
    AttractionExperimentGroup.CONTROL, AttractionExperimentGroup.TEST -> true
    else -> false
}

object AttractionExperiment {
    fun isSwitchPrettyControlVisible() = App.get().options.attractionExperimentGroup.isSwitchPrettyControlVisible()

    fun doClickAction(unknownGroupAction: () -> Unit, switchAction: () -> Unit, blockedAction: () -> Unit)
            = when(App.get().options.attractionExperimentGroup) {
        AttractionExperimentGroup.CONTROL -> switchAction()
        AttractionExperimentGroup.TEST -> {
            if (App.get().profile.premium) {
                switchAction()
            } else {
                blockedAction()
            }
        }
        else -> unknownGroupAction()
    }
}