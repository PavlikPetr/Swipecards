package com.topface.topface.experiments.feed_design

import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.TabbedLikesFragment
import com.topface.topface.ui.fragments.feed.likes.LikesFragment

/**
 * Фабрика генерирующая нужный фрагмент лайков
 * Created by m.bayutin on 08.02.17.
 */
class LikesFeedFactory {
    fun construct(): BaseFragment = when(DialogsAndLikesFeedDesigned.getDesignVersion()) {
            DialogsAndLikesFeedDesigned.NEW_DIALOGS_ALL_TABS,
            DialogsAndLikesFeedDesigned.NEW_DIALOGS_AND_SINGLE_TAB,
            DialogsAndLikesFeedDesigned.VERY_OLD_DESIGN -> TabbedLikesFragment()
            else -> LikesFragment()
        }
}