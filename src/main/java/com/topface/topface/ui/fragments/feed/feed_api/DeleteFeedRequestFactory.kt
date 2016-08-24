package com.topface.topface.ui.fragments.feed.feed_api

import android.content.Context
import android.os.Bundle
import com.topface.topface.requests.*
import com.topface.topface.utils.config.FeedsCache

/**
 * Конструктор запросов на удаление итемов в различных фидах
 * Created by tiberal on 08.08.16.
 */
class DeleteFeedRequestFactory(private val mContext: Context) : IRequestFactory {

    companion object {
        val USER_ID_FOR_DELETE = "user_id_for_delete"
        val FEED_TYPE = "feed_type"
    }

    override fun construct(arg: Bundle?) = arg?.let {
        val id = it.getStringArrayList(USER_ID_FOR_DELETE)
        when (it.getSerializable(FEED_TYPE) as FeedsCache.FEEDS_TYPE) {
            FeedsCache.FEEDS_TYPE.DATA_ADMIRATION_FEEDS -> DeleteAdmirationsRequest(id, mContext)
            FeedsCache.FEEDS_TYPE.DATA_BLACK_LIST_FEEDS -> DeleteBlackListRequest(id, mContext)
            FeedsCache.FEEDS_TYPE.DATA_BOOKMARKS_FEEDS -> DeleteBookmarksRequest(id, mContext)
            FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS -> DeleteDialogsRequest(id, mContext)
            FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS -> DeleteLikesRequest(id, mContext)
            FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS -> DeleteMutualsRequest(id, mContext)
            FeedsCache.FEEDS_TYPE.DATA_VISITORS_FEEDS -> DeleteVisitorsRequest(id, mContext)
            else -> {
                null
            }
        }
    }
}
