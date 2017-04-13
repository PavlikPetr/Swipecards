package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.UForeverAloneContactsListItemBinding
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.UForeverAloneStubItem
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Created by tiberal on 05.12.16.
 * Компонент для итема оповещающего пользователя, что у него нет ни симпатий, ни восхищений
 */
class UForeverAloneContactsListItemComponent(private val mNavigator: IFeedNavigator) : AdapterComponent<UForeverAloneContactsListItemBinding, UForeverAloneStubItem>() {
    override val itemLayout: Int
        get() = R.layout.u_forever_alone_contacts_list_item
    override val bindingClass: Class<UForeverAloneContactsListItemBinding>
        get() = UForeverAloneContactsListItemBinding::class.java

    override fun bind(binding: UForeverAloneContactsListItemBinding, data: UForeverAloneStubItem?, position: Int) {
        binding.root.setOnClickListener {
            mNavigator.showDating()
        }
    }
}