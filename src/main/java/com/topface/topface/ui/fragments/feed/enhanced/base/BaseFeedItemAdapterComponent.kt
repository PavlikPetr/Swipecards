package com.topface.topface.ui.fragments.feed.enhanced.base

import android.databinding.ViewDataBinding
import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.MultiselectionController
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import javax.inject.Inject

/**
 * База компонентов адаптера, для итема фида. Может клики и подсветку
 * Created by tiberal on 02.03.17.
 */
abstract class BaseFeedItemAdapterComponent<T : ViewDataBinding, D : FeedItem>(val click: (View?) -> Unit,
                                                                               val longClick: (View?) -> Unit) : AdapterComponent<T, D>() {

    @Inject lateinit var mNavigator: IFeedNavigator
    @Inject lateinit var mMultiselectionController: MultiselectionController<D>

    override fun bind(binding: T, data: D?, position: Int) {
        data?.let {
            attachViewModel(binding, it)
            binding.executePendingBindings()
            handleHighlight(binding, data)
            with(binding.root) {
                setOnClickListener {
                    click(it)
                }
                setOnLongClickListener {
                    longClick(it)
                    true
                }
            }
        }
    }

    abstract fun attachViewModel(binding: T, data: D)

    private fun handleHighlight(binding: T, data: D?) {
        binding.root.isSelected = false
        if (data?.unread ?: false) {
            binding.root.setBackgroundResource(R.drawable.new_feed_list_item_selector)
        } else {
            binding.root.setBackgroundResource(R.drawable.feed_list_item_selector)
        }
        if (data != null) {
            binding.root.isSelected = mMultiselectionController.mSelected.contains(data)
        }
    }
}