package com.topface.topface.ui.fragments.feed.enhanced.fans

import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedBookmark
import com.topface.topface.data.FeedItem
import com.topface.topface.databinding.NewFeedItemSimpleTimeBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.fans.FansComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemAdapterComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemViewModel


class FansAdapterComponent(click: (View?) -> Unit, longClick: (View?) -> Unit)
    : BaseFeedItemAdapterComponent<NewFeedItemSimpleTimeBinding, FeedBookmark>(click, longClick) {

    override val itemLayout = R.layout.new_feed_item_simple_time
    override val bindingClass = NewFeedItemSimpleTimeBinding::class.java

    init {
        ComponentManager.obtainComponent(FansComponent::class.java).inject(this)
    }

    override fun attachViewModel(binding: NewFeedItemSimpleTimeBinding, data: FeedBookmark) {
        binding.viewModel = FansItemViewModel(data, mNavigator,
                isActionModeEnabled = { mMultiselectionController.mSelected.isNotEmpty() }) { click(binding.root) } as BaseFeedItemViewModel<FeedItem>
    }
}