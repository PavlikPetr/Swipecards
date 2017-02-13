package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.data.User
import com.topface.topface.databinding.PhotoblogItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.IPopoverControl
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PhotoBlogItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент итема фотоленты
 * Created by ppavlik on 11.01.17.
 */

class PhotoBlogItemComponent(private val mNavigator: IFeedNavigator,
                             private val mPopoverControl: IPopoverControl) : AdapterComponent<PhotoblogItemBinding, FeedPhotoBlog>() {
    companion object {
        const val PLC = "geo_with_photoblog"
    }

    override val itemLayout: Int
        get() = R.layout.photoblog_item
    override val bindingClass: Class<PhotoblogItemBinding>
        get() = PhotoblogItemBinding::class.java

    override fun bind(binding: PhotoblogItemBinding, data: FeedPhotoBlog?, position: Int) {
        data?.let {
            binding.viewModel = PhotoBlogItemViewModel(it.user.photo,
                    if (it.user.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small) {
                mPopoverControl.closeByUser()
                if (App.get().profile.uid == it.user.id) {
                    mNavigator.showOwnProfile()
                } else {
                    mNavigator.showProfile(it, PLC)
                }
            }
        }
    }
}