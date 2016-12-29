package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.dialog_adapter_components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.DialogContactsItemBinding
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogContactsItemViewModel
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogContactsStubItem
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogTypeProvider
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.ILifeCycle

/**
 * Компонент хедера диалогов с симпатиями/восхищениями. Начинает новую переписку.
 * Created by tiberal on 01.12.16.
 */
class ContactsItemComponent(private val mNavigator: IFeedNavigator, private val mContext: Context, private val mApi: FeedApi)
    : AdapterComponent<DialogContactsItemBinding, DialogContactsStubItem>(), ILifeCycle {
    override val itemLayout: Int
        get() = R.layout.dialog_contacts_item
    override val bindingClass: Class<DialogContactsItemBinding>
        get() = DialogContactsItemBinding::class.java
    private var mModel: DialogContactsItemViewModel? = null
    private lateinit var mAdapter: CompositeAdapter
    private val mContactsListItemComponent by lazy { ContactsListItemComponent(mApi, mNavigator) }

    override fun bind(binding: DialogContactsItemBinding, data: DialogContactsStubItem?, position: Int) {
        data?.let {
            with(binding.giftsList) {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                mAdapter = CompositeAdapter(DialogTypeProvider()) { Bundle() }
                        .addAdapterComponent(mContactsListItemComponent)
                        .addAdapterComponent(GoDatingContactsListItemComponent(mNavigator))
                        .addAdapterComponent(UForeverAloneContactsListItemComponent(mNavigator))
                adapter = mAdapter
            }
            mModel = DialogContactsItemViewModel(mContext, it, mApi, mAdapter.updateObservable)
            binding.model = mModel
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mModel?.onActivityResult(requestCode, resultCode, data)
        mContactsListItemComponent.onActivityResult(requestCode, resultCode, data)
    }

    override fun release() {
        mModel?.release()
    }

}