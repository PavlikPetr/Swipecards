package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual

import android.view.View
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.databinding.NewFeedItemCardBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.mutual.MutualComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemAdapterComponent

class MutualAdapterComponent(click: (View?) -> Unit, longClick: (View?) -> Unit)
    : BaseFeedItemAdapterComponent<NewFeedItemCardBinding, FeedBookmark>(click, {}) {

    override val itemLayout = R.layout.new_feed_item_card
    override val bindingClass = NewFeedItemCardBinding::class.java

    init {
        ComponentManager.obtainComponent(MutualComponent::class.java).inject(this)
    }

    override fun attachViewModel(binding: NewFeedItemCardBinding, data: FeedBookmark) {
        binding.viewModel = MutualItemViewModel(data, mNavigator)
    }
}