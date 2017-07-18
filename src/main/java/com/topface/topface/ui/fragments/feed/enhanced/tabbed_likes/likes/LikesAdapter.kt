package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.LikesCardsItemBinding
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.BaseAdapter


/**
 * Created by ppavlik on 17.07.17.
 * Адаптер для карточного вида симпатий
 */
class LikesAdapter : BaseAdapter<LikesCardsItemBinding, FeedBookmark>() {
    override val layout = R.layout.likes_cards_item

    override fun bind(binding: LikesCardsItemBinding?, data: FeedBookmark?) {
        data?.let {
            binding?.viewModel = LikesItemViewModel(it)
        }
    }
}