package com.topface.topface.experiments.feed_design

import com.topface.topface.App

/**
 * Контролирует версию дизайна лайков
 * Created by m.bayutin on 08.02.17.
 */
object DialogsAndLikesFeedDesigned : IDesigned<Int> {
    const val VERY_OLD_DESIGN = 0
    const val NEW_DIALOGS_AND_SINGLE_TAB = 1
    const val NEW_DIALOG_NO_TAB = 2
    const val NEW_DIALOGS_ALL_TABS = 3
    override fun getDesignVersion() = App.get().options.dialogDesignVersion
    @JvmStatic fun getDesignVersionJava() = getDesignVersion()
}