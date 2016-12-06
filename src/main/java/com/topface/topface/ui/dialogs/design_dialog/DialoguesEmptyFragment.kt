package com.topface.topface.ui.dialogs.design_dialog

import android.view.View

import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyDialoguesBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate

/**
 * Фрагмент заглушки пустых диалогов
 * Created by siberia87 on 06.12.16.
 */
class DialoguesEmptyFragment() : AbstractDialogFragment() {

    private lateinit var mBinding: LayoutEmptyDialoguesBinding

    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mViewModel by lazy {
        DialoguesEmptyFragmentViewModel(mNavigator)
    }

    override fun initViews(root: View) {
        mBinding = LayoutEmptyDialoguesBinding.bind(root)
        mBinding.setViewModel(mViewModel)
    }

    override fun getDialogLayoutRes() = R.layout.layout_empty_dialogues

    override fun isModalDialog() = false
}