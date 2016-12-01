package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.FeedItemDialogNewBinding
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Adapter component for pyre
 * Created by tiberal on 30.11.16.
 */
class DialogItemComponent(private val mNavigator: IFeedNavigator) : AdapterComponent<FeedItemDialogNewBinding, FeedDialog>() {

    override val itemLayout: Int
        get() = R.layout.feed_item_dialog_new
    override val bindingClass: Class<FeedItemDialogNewBinding>
        get() = FeedItemDialogNewBinding::class.java

    override fun bind(binding: FeedItemDialogNewBinding, data: FeedDialog?, position: Int) {
        data?.let {
            binding.model = DialogItemNewViewModel(it, mNavigator)
        }
    }

}