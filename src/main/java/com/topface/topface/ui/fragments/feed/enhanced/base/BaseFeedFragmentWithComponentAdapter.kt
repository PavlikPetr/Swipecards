package com.topface.topface.ui.fragments.feed.enhanced.base

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.databinding.NewFeedFragmentBaseBinding
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

/**
 * Created by ppavlik on 20.07.17.
 */
abstract class BaseFeedFragmentWithComponentAdapter<T : FeedItem> : BaseFeedFragment<T, CompositeAdapter, NewFeedFragmentBaseBinding>() {
    override val res: Int
        get() = R.layout.new_feed_fragment_base

    override fun initScreenView(binding: NewFeedFragmentBaseBinding) {
        with(binding.feedList) {
            itemAnimator = null
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    override fun onDestroyView() {
        mBinding.feedList.stopScroll()
        super.onDestroyView()
    }

    override fun getViewPosition(view: View?) = mBinding.feedList.layoutManager.getPosition(view)
}