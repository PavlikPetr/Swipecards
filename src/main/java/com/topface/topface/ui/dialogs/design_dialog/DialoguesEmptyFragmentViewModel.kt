package com.topface.topface.ui.dialogs.design_dialog

import com.topface.topface.databinding.LayoutEmptyDialoguesBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.viewModels.BaseViewModel

/**
 * VM пустых диалогов
 * Created by siberia87 on 06.12.16.
 */
class DialoguesEmptyFragmentViewModel(binding: LayoutEmptyDialoguesBinding,
                                      private val mNavigator: FeedNavigator) :
        BaseViewModel<LayoutEmptyDialoguesBinding>(binding) {

    fun startDating() {
        mNavigator.showDating()
    }
}