package com.topface.topface.api

import android.os.Bundle
import com.topface.topface.api.requests.BaseScruffyRequest
import com.topface.topface.api.requests.DeleteVisitorsRequest
import com.topface.topface.api.responses.Completed

class DeleteFeedRequestFactory : IRequestFactory<Completed> {

    companion object {
        const val USER_ID_FOR_DELETE = "user_id_for_delete"
        const val FEED_TYPE = "feed_type"
    }

    override fun construct(arg: Bundle): BaseScruffyRequest<Completed> {
        val id = arg.getStringArrayList(USER_ID_FOR_DELETE)
        return DeleteVisitorsRequest(id)
        /*when (it.getSerializable(FEED_TYPE) as FeedsCache.FEEDS_TYPE) {
            FeedsCache.FEEDS_TYPE.DATA_VISITORS_FEEDS -> DeleteVisitorsRequest(id)

            FeedsCache.FEEDS_TYPE.DATA_ADMIRATION_FEEDS, FeedsCache.FEEDS_TYPE.DATA_BLACK_LIST_FEEDS,
            FeedsCache.FEEDS_TYPE.DATA_BOOKMARKS_FEEDS, FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS,
            FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS, FeedsCache.FEEDS_TYPE.DATA_MUTUALS_FEEDS -> null
        }*/
    }
}