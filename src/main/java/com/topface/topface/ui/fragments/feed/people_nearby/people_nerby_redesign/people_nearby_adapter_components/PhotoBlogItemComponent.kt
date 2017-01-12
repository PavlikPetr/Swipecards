package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components

import com.topface.topface.R
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.data.User
import com.topface.topface.databinding.ItemEmptyPeopleNearbyBinding
import com.topface.topface.databinding.PhotoblogItemBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyList
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyEmptyViewModel
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PhotoBlogItem
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PhotoBlogItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

/**
 * Компонент итема фотоленты
 * Created by ppavlik on 11.01.17.
 */

class PhotoBlogItemComponent(private val mNavigator: IFeedNavigator) : AdapterComponent<PhotoblogItemBinding, FeedPhotoBlog>() {
    override val itemLayout: Int
        get() = R.layout.photoblog_item
    override val bindingClass: Class<PhotoblogItemBinding>
        get() = PhotoblogItemBinding::class.java

    override fun bind(binding: PhotoblogItemBinding, data: FeedPhotoBlog?, position: Int) {
        data?.let {
            binding.viewModel = PhotoBlogItemViewModel(it.user.photo,
                    if (it.user.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small) {
                mNavigator.showChat(it.user, null)
            }
        }
    }
}