package com.topface.topface.ui.fragments.feed.photoblog

import android.view.View
import com.topface.topface.App
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.debug.FuckingVoodooMagic
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * VM for photoblog
 * Created by tiberal on 05.09.16.
 */
class PhotoblogFragmentViewModel(binding: FragmentFeedBaseBinding, private val mNavigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedPhotoBlog>(binding, mNavigator, api) {

    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)

    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.PHOTOBLOG

    override val itemClass: Class<FeedPhotoBlog>
        get() = FeedPhotoBlog::class.java

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.UNKNOWN_TYPE

    override val isNeedCacheItems: Boolean
        get() = false

    override fun itemClick(view: View?, itemPosition: Int, data: FeedPhotoBlog?) =
            if (App.get().profile.uid != data?.user?.id) {
                mNavigator.showOwnProfile()
            } else {
                super.itemClick(view, itemPosition, data)
            }

    /**
     * т.к. при обновлении мы удаляем все итемы из списка и сетим новые, то в таком раскладе
     * notifyItemRangeInserted не работает, по этому обновляем весь лист тут. Если нужно будет добавлять
     * тоолько новые нужно будет изменить правило сравнения в considerDuplicates
     */
    @FuckingVoodooMagic
    override fun onRefreshed() {
        binding?.feedList?.adapter?.let {
            it.notifyDataSetChanged()
        }
    }
}