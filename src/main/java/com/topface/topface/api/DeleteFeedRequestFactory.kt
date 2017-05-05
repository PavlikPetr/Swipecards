package com.topface.topface.api

import android.os.Bundle
import com.topface.topface.api.requests.BaseScruffyRequest
import com.topface.topface.api.requests.DeleteBookmarksRequest
import com.topface.topface.api.requests.DeleteVisitorsRequest
import com.topface.topface.api.responses.Completed
import com.topface.topface.utils.config.FeedsCache

class DeleteFeedRequestFactory : IRequestFactory<Completed> {

    companion object {
        const val USER_ID_FOR_DELETE = "user_id_for_delete"
        const val FEED_TYPE = "feed_type"
    }

    override fun construct(arg: Bundle): BaseScruffyRequest<Completed> {
        val id = arg.getStringArrayList(USER_ID_FOR_DELETE)
        val request = when (arg.getSerializable(FEED_TYPE) as FeedsCache.FEEDS_TYPE) {
            FeedsCache.FEEDS_TYPE.DATA_VISITORS_FEEDS -> DeleteVisitorsRequest(id)

            FeedsCache.FEEDS_TYPE.DATA_FANS_FEEDS -> TODO()
            FeedsCache.FEEDS_TYPE.UNKNOWN_TYPE -> TODO()
            FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS -> TODO()
            FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS -> TODO()
            FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS -> TODO()
            FeedsCache.FEEDS_TYPE.DATA_ADMIRATION_FEEDS -> TODO()
            FeedsCache.FEEDS_TYPE.DATA_BOOKMARKS_FEEDS -> TODO()
            FeedsCache.FEEDS_TYPE.DATA_BLACK_LIST_FEEDS -> TODO()
        }
        return request
    }
}