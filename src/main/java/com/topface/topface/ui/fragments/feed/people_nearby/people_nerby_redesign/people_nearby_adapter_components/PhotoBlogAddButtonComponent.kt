package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.databinding.ItemEmptyPeopleNearbyBinding
import com.topface.topface.databinding.PhotoblogItemBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.*
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

/**
 * Компонент итема "+" для постановки в ленту
 * Created by ppavlik on 11.01.17.
 */

class PhotoBlogAddButtonComponent(private val mNavigator: IFeedNavigator,
                                  private val mPopoverControl: IPopoverControl) : AdapterComponent<PhotoblogItemBinding, PhotoBlogAdd>() {
    override val itemLayout: Int
        get() = R.layout.photoblog_item
    override val bindingClass: Class<PhotoblogItemBinding>
        get() = PhotoblogItemBinding::class.java

    private lateinit var mViewModel: PhotoBlogAddButtonViewModel

    override fun bind(binding: PhotoblogItemBinding, data: PhotoBlogAdd?, position: Int) {
        data?.let {
            mViewModel = PhotoBlogAddButtonViewModel(mNavigator, App.get().profile, mPopoverControl)
            binding.viewModel = mViewModel.photoBlogViewModel
        }
    }

    override fun release() {
        super.release()
        mViewModel.release()
    }
}