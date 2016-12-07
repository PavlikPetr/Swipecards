package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.GoDatingContactsListItemBinding
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.GoDatingContactsStubItem
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент последнего итема в контактах. Отсылает в дейтинг.
 * Created by tiberal on 05.12.16.
 */
class GoDatingContactsListItemComponent(private val mNavigator: IFeedNavigator) : AdapterComponent<GoDatingContactsListItemBinding, GoDatingContactsStubItem>() {

    override val itemLayout: Int
        get() = R.layout.go_dating_contacts_list_item
    override val bindingClass: Class<GoDatingContactsListItemBinding>
        get() = GoDatingContactsListItemBinding::class.java

    override fun bind(binding: GoDatingContactsListItemBinding, data: GoDatingContactsStubItem?, position: Int) {
        binding.root.setOnClickListener {
            mNavigator.showDating()
        }
    }
}