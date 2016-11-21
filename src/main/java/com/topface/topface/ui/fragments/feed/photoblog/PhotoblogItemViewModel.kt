package com.topface.topface.ui.fragments.feed.photoblog

import com.topface.topface.App
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.databinding.FeedPhotoblogItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by tiberal on 05.09.16.
 * Модель и тема для фотоблога
 */
class PhotoblogItemViewModel(binding: FeedPhotoblogItemBinding, item: FeedPhotoBlog, private val mNavigator: IFeedNavigator,
                             isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedPhotoblogItemBinding, FeedPhotoBlog>(binding, item, mNavigator, isActionModeEnabled) {

    override val feed_type: String
        get() = "Photoblog"

    override val text: String?
        get() = item.user.status

    override fun onAvatarClickActionModeDisabled() =
            if (App.get().profile.uid == item.user?.id) {
                mNavigator.showOwnProfile()
            } else {
                super.onAvatarClickActionModeDisabled()
            }

}