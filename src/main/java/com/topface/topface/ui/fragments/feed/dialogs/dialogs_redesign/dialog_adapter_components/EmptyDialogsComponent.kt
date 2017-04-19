package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyDialogItemBinding
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.EmptyDialogsStubItem
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для отображения заглушки, когда симпатии емсть, а диалогов нет
 * Created by tiberal on 01.12.16.
 */
class EmptyDialogsComponent : AdapterComponent<LayoutEmptyDialogItemBinding, EmptyDialogsStubItem>() {

    override val itemLayout: Int
        get() = R.layout.layout_empty_dialog_item
    override val bindingClass: Class<LayoutEmptyDialogItemBinding>
        get() = LayoutEmptyDialogItemBinding::class.java

    override fun bind(binding: LayoutEmptyDialogItemBinding, data: EmptyDialogsStubItem?, position: Int) {}
}