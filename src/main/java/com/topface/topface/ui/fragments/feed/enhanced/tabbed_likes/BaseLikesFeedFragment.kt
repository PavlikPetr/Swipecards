package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.databinding.NewFeedFragmentBaseBinding
import com.topface.topface.ui.fragments.feed.dialogs.PopupMenuFragment
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragment
import com.topface.topface.utils.extensions.getInt

/**
 * Created by ppavlik on 13.07.17.
 * Базовый фрагмент для фидового списка "взаимно" и "восхищения"
 */
abstract class BaseLikesFeedFragment : BaseFeedFragment<FeedBookmark>() {

    override fun initScreenView(binding: NewFeedFragmentBaseBinding) {
        with(binding.feedList) {
            itemAnimator = null
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(R.integer.feed_like_grid_column_count.getInt(),
                    StaggeredGridLayoutManager.VERTICAL)
            addItemDecoration(CardItemDecoration())
            adapter = mAdapter
        }
    }

    fun sympathyItemLongClick(view: View?, @PopupMenuFragment.MenuPopupType popupMenuPopupType: Long) {
        val itemPosition = mBinding.feedList.layoutManager.getPosition(view)
        val data = mAdapter.data[itemPosition] as FeedBookmark
        mNavigator.showDialogpopupMenu(data, popupMenuPopupType)
    }
}