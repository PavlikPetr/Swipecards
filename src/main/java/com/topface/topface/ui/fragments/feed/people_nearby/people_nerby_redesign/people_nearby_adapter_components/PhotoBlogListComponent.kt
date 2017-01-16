package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.PhotoblogListBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PgotoblogListTypeProvider
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PhotoBlogList
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PhotoBlogListViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.ILifeCycle

/**
 * Компонент фотоленты с горизонтальным скролом
 * Created by ppavlik on 11.01.17.
 */

class PhotoBlogListComponent(private val mContext: Context,
                             private val mApi: FeedApi,
                             private val mNavigator: IFeedNavigator) : AdapterComponent<PhotoblogListBinding, PhotoBlogList>(), ILifeCycle {
    private lateinit var mAdapter: CompositeAdapter
    private val mViewModel: PhotoBlogListViewModel by lazy { PhotoBlogListViewModel(mApi) }

    override val itemLayout: Int
        get() = R.layout.photoblog_list
    override val bindingClass: Class<PhotoblogListBinding>
        get() = PhotoblogListBinding::class.java

    override fun bind(binding: PhotoblogListBinding, data: PhotoBlogList?, position: Int) {
        with(binding) {
            photoblogList.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            mAdapter = CompositeAdapter(PgotoblogListTypeProvider()) { Bundle() }
                    .addAdapterComponent(PhotoBlogItemComponent(mNavigator))
                    .addAdapterComponent(PhotoBlogAddButtonComponent(mNavigator))
            photoblogList.adapter = mAdapter
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
    }
}