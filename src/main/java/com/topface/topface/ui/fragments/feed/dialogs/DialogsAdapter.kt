package com.topface.topface.ui.fragments.feed.dialogs

import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.FeedItemDialogBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by tiberal on 18.09.16.
 */
class DialogsAdapter(private val mNavigator: IFeedNavigator) : BaseFeedAdapter<FeedItemDialogBinding, FeedDialog>() {

    override fun getItemLayout() = R.layout.feed_item_dialog

    override fun bindData(binding: FeedItemDialogBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let {
            binding.model = DialogsItemViewModel(it, getDataItem(position), mNavigator) { isActionModeEnabled }
        }
    }
}