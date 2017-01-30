package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.PhotoblogListBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.*
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.extensions.getDimen

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
    private var mAdapter: CompositeAdapter? = null
    private var mPhotoblogListBinding: PhotoblogListBinding? = null
    private var mRecyclerView: RecyclerView? = null
    private val mViewModel: PhotoBlogListViewModel by lazy {
        PhotoBlogListViewModel(mApi) {
            mPhotoblogListBinding?.let {
                it.root.post { mSize.size(Size(it.root.measuredHeight, it.root.measuredWidth))
                    Debug.error("!!!!!!!!!!!!!!!!!!!!!mPhotoblogListBinding sizes = ${mPhotoblogListBinding?.root?.measuredHeight}")}
            }
        }
    }
    private val mScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            mPopoverControl.closeByUser()
        }
    }

    override val itemLayout: Int
        get() = R.layout.photoblog_list
    override val bindingClass: Class<PhotoblogListBinding>
        get() = PhotoblogListBinding::class.java

    override fun bind(binding: PhotoblogListBinding, data: PhotoBlogList?, position: Int) {
        with(binding) {
            photoblogList.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                    val itemPosition = (view?.layoutParams as? RecyclerView.LayoutParams)?.viewAdapterPosition ?: 0
                    outRect?.apply {
                        set(
                                // нулевой итем имеет отступ отличный от остальных
                                if (itemPosition == 0) {
                                    R.dimen.photoblog_add_button_margin_left
                                } else {
                                    R.dimen.photoblog_item_margin_left
                                }.getDimen().toInt(),
                                R.dimen.photoblog_item_margin_top.getDimen().toInt(),
                                R.dimen.photoblog_item_margin_right.getDimen().toInt(),
                                R.dimen.photoblog_item_margin_bottom.getDimen().toInt())
                    }
                }
            })
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
        mAdapter?.releaseComponents()
        mRecyclerView?.removeOnScrollListener(mScrollListener)
    }
}