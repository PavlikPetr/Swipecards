package com.topface.topface.api

import android.os.Bundle
import android.text.TextUtils
import com.topface.topface.api.requests.FeedRequest
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.utils.Utils

class FeedRequestFactory : IFeedRequestFactory {

    companion object {
        const val FROM = "from"
        const val TO = "to"
        const val SERVICE = "service"
        const val LEAVE = "leave"
        const val HISTORY_LOAD_FLAG = "history_load_flag"
        const val PULL_TO_REF_FLAG = "pull_to_refresh_flag"
        const val UNREAD_STATE = "unread_state"
    }

    override fun <P : IBaseFeedResponse> construct(arg: Bundle, responseClass: Class<P>): FeedRequest<P> {
        val service = getServiceName(arg.getSerializable(FeedRequestFactory.SERVICE) as FeedService)
        val unreadState = arg.getParcelable<UnreadStatePair>(FeedRequestFactory.UNREAD_STATE)
        val leave = arg.getBoolean(FeedRequestFactory.LEAVE)
        val from = arg.getString(FeedRequestFactory.FROM, Utils.EMPTY)
        val to = arg.getString(FeedRequestFactory.TO, Utils.EMPTY)

        val request = FeedRequest(service, unreadState, leave, respClass = responseClass)

        if (arg.getBoolean(FeedRequestFactory.PULL_TO_REF_FLAG) && !TextUtils.isEmpty(from)) {
            request.from = from
        }
        if (arg.getBoolean(FeedRequestFactory.HISTORY_LOAD_FLAG) && !TextUtils.isEmpty(to)) {
            request.to = to
        }
        return request
    }


    enum class FeedService {
        DIALOGS, LIKES, MUTUAL, VISITORS, BLACK_LIST, BOOKMARKS, FANS, ADMIRATIONS, GEO, PHOTOBLOG
    }


    fun getServiceName(service: FeedService) = when (service) {
        FeedService.DIALOGS -> "dialog.getList"
        FeedService.LIKES -> "like.getList"
        FeedService.MUTUAL -> "mutual.getList"
        FeedService.VISITORS -> "visitor.getList"
        FeedService.BLACK_LIST -> "blacklist.getList"
        FeedService.PHOTOBLOG -> "photofeed.getList"
        FeedService.FANS -> "fan.getList"
        FeedService.BOOKMARKS -> "bookmark.getList"
        else -> "admiration.getList"
    }

}