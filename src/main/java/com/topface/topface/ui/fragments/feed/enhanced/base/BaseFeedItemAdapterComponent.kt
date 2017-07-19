package com.topface.topface.ui.fragments.feed.enhanced.base

import android.databinding.ViewDataBinding
import android.view.View
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
            binding.root.isSelected = mMultiselectionController.mSelected.contains(data)
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

}