package com.topface.topface.ui.add_to_photo_blog.adapter_components

import android.databinding.ObservableInt
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.topface.topface.R
import com.topface.topface.databinding.ItemAddToPhotoBlogPhotoListBinding
import com.topface.topface.ui.add_to_photo_blog.AddToPhotoBlogRedesignActivity
import com.topface.topface.ui.add_to_photo_blog.PhotoListItem
import com.topface.topface.ui.add_to_photo_blog.PhotoListItemViewModel
import com.topface.topface.ui.add_to_photo_blog.PhotoTypeProvider
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.adapter_utils.create16Left8TotalMargin
import com.topface.topface.utils.extensions.appContext

/**
 * Компонент со списком фоток пользователя
 * Created by mbayutin on 10.01.17.
 */
class PhotoListComponent(val lastSelectedPhotoId: ObservableInt, private val mApi: FeedApi)
    : AdapterComponent<ItemAddToPhotoBlogPhotoListBinding, PhotoListItem>(), ILifeCycle {
    private var mAdapter: CompositeAdapter? = null
    private var mViewModel: PhotoListItemViewModel? = null
    private var mSavedState: Parcelable? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    override val itemLayout: Int
        get() = R.layout.item_add_to_photo_blog_photo_list
    override val bindingClass: Class<ItemAddToPhotoBlogPhotoListBinding>
        get() = ItemAddToPhotoBlogPhotoListBinding::class.java

    override fun bind(binding: ItemAddToPhotoBlogPhotoListBinding, data: PhotoListItem?, position: Int) {
        with(binding) {
            mLayoutManager = LinearLayoutManager(binding.appContext(), LinearLayoutManager.HORIZONTAL, false)
            content.layoutManager = mLayoutManager
            mAdapter = CompositeAdapter(PhotoTypeProvider()) { Bundle() }
                    .addAdapterComponent(PhotoComponent(lastSelectedPhotoId))
            content.adapter = mAdapter
            content.addItemDecoration(create16Left8TotalMargin())
        }

        mAdapter?.let {
            mViewModel = PhotoListItemViewModel(
                    mApi,
                    it.updateObservable,
                    lastSelectedPhotoId,
                    mSavedState
            )
            binding.viewModel = mViewModel
        }
    }

    override fun onSavedInstanceState(state: Bundle) = mLayoutManager?.let {
        state.putParcelable(AddToPhotoBlogRedesignActivity.SELECTED_PHOTO_POSITION, it.onSaveInstanceState())
    } ?: Unit

    override fun onRestoreInstanceState(state: Bundle) {
        mSavedState = state.getParcelable(AddToPhotoBlogRedesignActivity.SELECTED_PHOTO_POSITION)
    }

    override fun release() {
        mViewModel?.release()
        mAdapter?.releaseComponents()
    }
}