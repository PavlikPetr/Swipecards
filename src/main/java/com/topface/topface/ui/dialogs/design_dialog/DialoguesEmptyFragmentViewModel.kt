package com.topface.topface.ui.dialogs.design_dialog

import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * VM пустых диалогов
 * Created by siberia87 on 06.12.16.
 */
class DialoguesEmptyFragmentViewModel(private val mNavigator: IFeedNavigator) {

    fun startDating() = mNavigator.showDating()
}