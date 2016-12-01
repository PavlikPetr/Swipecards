package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyDialogItemBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для отображения
 * Created by tiberal on 01.12.16.
 */
class EmptyDialogsComponent : AdapterComponent<LayoutEmptyDialogItemBinding,EmptyDialogsItem>(){

    override val itemLayout: Int
        get() = R.layout.layout_empty_dialog_item
    override val bindingClass: Class<LayoutEmptyDialogItemBinding>
        get() = LayoutEmptyDialogItemBinding::class.java

    override fun bind(binding: LayoutEmptyDialogItemBinding, data: EmptyDialogsItem?, position: Int) {
    }
}