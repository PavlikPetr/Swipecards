package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.DialogContactsListItemBinding
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogContactsListItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для итема контакта(аватарка/имя)
 * Created by tiberal on 05.12.16.
 */
class ContactsListItemComponent(private val mNavigator: IFeedNavigator) :
        AdapterComponent<DialogContactsListItemBinding, DialogContactsItem>() {

    override val itemLayout: Int
        get() = R.layout.dialog_contacts_list_item
    override val bindingClass: Class<DialogContactsListItemBinding>
        get() = DialogContactsListItemBinding::class.java

    override fun bind(binding: DialogContactsListItemBinding, data: DialogContactsItem?, position: Int) {
        data?.let {
            binding.model = DialogContactsListItemViewModel(mNavigator,data)
        }
    }

}