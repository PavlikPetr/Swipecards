package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.DialogContactsItemBinding
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogContactsItemViewModel
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogContactsStubItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogTypeProvider
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

/**
 * Компонент хедера диалогов с симпатиями/восхищениями. Начинает новую переписку.
 * Created by tiberal on 01.12.16.
 */
class ContactsItemComponent(private val mNavigator: IFeedNavigator) : AdapterComponent<DialogContactsItemBinding, DialogContactsStubItem>() {
    override val itemLayout: Int
        get() = R.layout.dialog_contacts_item
    override val bindingClass: Class<DialogContactsItemBinding>
        get() = DialogContactsItemBinding::class.java

    override fun bind(binding: DialogContactsItemBinding, data: DialogContactsStubItem?, position: Int) {
        with(binding.giftsList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = CompositeAdapter(DialogTypeProvider()) { Bundle() }
                    .addAdapterComponent(ContactsListItemComponent(mNavigator))
                    .addAdapterComponent(GoDatingContactsListItemComponent(mNavigator))
                    .addAdapterComponent(UForeverAloneContactsListItemComponent(mNavigator))
        }
        binding.model = DialogContactsItemViewModel()
    }
}