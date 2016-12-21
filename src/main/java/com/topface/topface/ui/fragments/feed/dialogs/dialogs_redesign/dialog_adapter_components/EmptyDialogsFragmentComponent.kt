package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyDialoguesBinding
import com.topface.topface.ui.dialogs.design_dialog.DialoguesEmptyFragmentViewModel
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.EmptyDialogsFragmentStubItem
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент, для отображения пустых диалогов, вообще пустых, ничего нет.
 * Created by tiberal on 07.12.16.
 */
class EmptyDialogsFragmentComponent(private val mNavigator: IFeedNavigator)
    : AdapterComponent<LayoutEmptyDialoguesBinding, EmptyDialogsFragmentStubItem>() {
    override val itemLayout: Int
        get() = R.layout.layout_empty_dialogues
    override val bindingClass: Class<LayoutEmptyDialoguesBinding>
        get() = LayoutEmptyDialoguesBinding::class.java

    override fun bind(binding: LayoutEmptyDialoguesBinding, data: EmptyDialogsFragmentStubItem?, position: Int) {
        binding.setViewModel(DialoguesEmptyFragmentViewModel(mNavigator))
    }
}