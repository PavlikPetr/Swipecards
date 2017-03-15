package com.topface.topface.ui.fragments.feed.enhanced.visitors

import android.view.View
import com.topface.topface.R
import com.topface.topface.api.responses.Visitor
import com.topface.topface.data.FeedItem
import com.topface.topface.databinding.NewFeedItemSimpleTimeBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.visitors.VisitorsComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemAdapterComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemViewModel


class VisitorAdapterComponent(click: (View?) -> Unit, longClick: (View?) -> Unit)
    : BaseFeedItemAdapterComponent<NewFeedItemSimpleTimeBinding, Visitor>(click, longClick) {

    override val itemLayout = R.layout.new_feed_item_simple_time
    override val bindingClass = NewFeedItemSimpleTimeBinding::class.java

    init {
        ComponentManager.obtainComponent(VisitorsComponent::class.java).inject(this)
    }

    override fun attachViewModel(binding: NewFeedItemSimpleTimeBinding, data: Visitor) {
        binding.viewModel = VisitorsItemViewModel(data, mNavigator,
                isActionModeEnabled = { mMultiselectionController.mSelected.isNotEmpty() }) { click(binding.root) } as BaseFeedItemViewModel<FeedItem>
    }

}