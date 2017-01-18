package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.topface.topface.R
import com.topface.topface.databinding.PhotoblogListBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.*
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.ILifeCycle

/**
 * Компонент фотоленты с горизонтальным скролом
 * Created by ppavlik on 11.01.17.
 */

class PhotoBlogListComponent(private val mContext: Context,
                             private val mApi: FeedApi,
                             private val mNavigator: IFeedNavigator,
                             private val mPopoverControl: IPopoverControl,
                             private val mSize: IViewSize) : AdapterComponent<PhotoblogListBinding, PhotoBlogList>(),
        ILifeCycle {
    private lateinit var mAdapter: CompositeAdapter
    private lateinit var mPhotoblogListBinding: PhotoblogListBinding
    private lateinit var mRecyclerView: RecyclerView
    private val mViewModel: PhotoBlogListViewModel by lazy {
        PhotoBlogListViewModel(mApi) {
            mPhotoblogListBinding.root.post { mSize.size(Size(mPhotoblogListBinding.root.measuredHeight, mPhotoblogListBinding.root.measuredWidth)) }
        }
    }
    private val mScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            mPopoverControl.close()
        }
    }

    override val itemLayout: Int
        get() = R.layout.photoblog_list
    override val bindingClass: Class<PhotoblogListBinding>
        get() = PhotoblogListBinding::class.java

    override fun bind(binding: PhotoblogListBinding, data: PhotoBlogList?, position: Int) {
        with(binding) {
            mRecyclerView = photoblogList
            mPhotoblogListBinding = this
            photoblogList.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            mAdapter = CompositeAdapter(PgotoblogListTypeProvider()) { Bundle() }
                    .addAdapterComponent(PhotoBlogItemComponent(mNavigator, mPopoverControl))
                    .addAdapterComponent(PhotoBlogAddButtonComponent(mNavigator, mPopoverControl))
            photoblogList.adapter = mAdapter
            photoblogList.addOnScrollListener(mScrollListener)
            viewModel = mViewModel
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mViewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun release() {
        super.release()
        mViewModel.release()
        mAdapter.releaseComponents()
        mRecyclerView.removeOnScrollListener(mScrollListener)
    }
}