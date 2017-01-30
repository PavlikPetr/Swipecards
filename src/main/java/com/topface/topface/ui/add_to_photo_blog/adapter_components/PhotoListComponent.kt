package com.topface.topface.ui.add_to_photo_blog.adapter_components

import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.ItemAddToPhotoBlogPhotoListBinding
import com.topface.topface.ui.add_to_photo_blog.PhotoListItem
import com.topface.topface.ui.add_to_photo_blog.PhotoListItemViewModel
import com.topface.topface.ui.add_to_photo_blog.PhotoTypeProvider
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.appContext

/**
 * Компонент со списком фоток пользователя
 * Created by mbayutin on 10.01.17.
 */
class PhotoListComponent(val lastSelectedPhotoId: ObservableInt, private val mApi: FeedApi) : AdapterComponent<ItemAddToPhotoBlogPhotoListBinding, PhotoListItem>() {
    private var mAdapter: CompositeAdapter? = null
    private var mViewModel: PhotoListItemViewModel? = null
    override val itemLayout: Int
        get() = R.layout.item_add_to_photo_blog_photo_list
    override val bindingClass: Class<ItemAddToPhotoBlogPhotoListBinding>
        get() = ItemAddToPhotoBlogPhotoListBinding::class.java

    override fun bind(binding: ItemAddToPhotoBlogPhotoListBinding, data: PhotoListItem?, position: Int) {
        with(binding) {
            content.layoutManager = LinearLayoutManager(binding.appContext(), LinearLayoutManager.HORIZONTAL, false)
            mAdapter = CompositeAdapter(PhotoTypeProvider()) { Bundle() }
                    .addAdapterComponent(PhotoComponent(lastSelectedPhotoId))
            content.adapter = mAdapter
        }

        mAdapter?.let {
            mViewModel = PhotoListItemViewModel(mApi, it.updateObservable, lastSelectedPhotoId)
            binding.viewModel = mViewModel
        }
    }

    override fun release() {
        mViewModel?.release()
        mAdapter?.releaseComponents()
    }
}