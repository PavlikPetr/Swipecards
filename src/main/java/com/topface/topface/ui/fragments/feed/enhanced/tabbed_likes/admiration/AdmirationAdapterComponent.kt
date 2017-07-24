package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration

import android.view.View
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.databinding.NewFeedItemCardBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.admiration.AdmirationComponent
import com.topface.topface.di.feed.mutual.MutualComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemAdapterComponent
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual.MutualItemViewModel

class AdmirationAdapterComponent(click: (View?) -> Unit, longClick: (View?) -> Unit)
    : BaseFeedItemAdapterComponent<NewFeedItemCardBinding, FeedBookmark>(click, longClick) {

    override val itemLayout = R.layout.new_feed_item_card
    override val bindingClass = NewFeedItemCardBinding::class.java

    init {
        ComponentManager.obtainComponent(AdmirationComponent::class.java).inject(this)
    }

    override fun attachViewModel(binding: NewFeedItemCardBinding, data: FeedBookmark) {
        binding.viewModel = AdmirationItemViewModel(data, mNavigator)
    }
}