package com.topface.topface.experiments.feed_design

import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogsFragment

/**
 * Фабрика генерирующая нужный фрагмент диалогов
 * Created by m.bayutin on 09.02.17.
 */
class DialogsFeedFactory {
    fun construct(): BaseFragment = when(DialogsAndLikesFeedDesigned.getDesignVersion()) {
        DialogsAndLikesFeedDesigned.NEW_DIALOGS_ALL_TABS,
        DialogsAndLikesFeedDesigned.NEW_DIALOGS_AND_SINGLE_TAB,
        DialogsAndLikesFeedDesigned.NEW_DIALOG_NO_TAB -> DialogsFragment()
        else -> TabbedDialogsFragment()
    }
}