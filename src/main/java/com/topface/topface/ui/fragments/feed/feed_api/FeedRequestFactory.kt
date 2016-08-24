package com.topface.topface.ui.fragments.feed.feed_api

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.topface.topface.requests.FeedRequest
import com.topface.topface.utils.Utils

/**
 * Собираем фид реквест для фида
 * Created by tiberal on 09.08.16.
 */
class FeedRequestFactory(private var mContext: Context) : IRequestFactory {

    companion object {
        val FROM = "from"
        val TO = "to"
        val SERVICE = "service"
        val LEAVE = "leave"
        val HISTORY_LOAD_FLAG = "history_load_flag"
        val PULL_TO_REF_FLAG = "pull_to_refresh_flag"
        val UNREAD_STATE = "unread_state"
    }

    override fun construct(arg: Bundle?) = arg?.let {
        val request = FeedRequest(it.getSerializable(SERVICE) as FeedRequest.FeedService?, mContext)
        request.setPreviousUnreadState(it.getParcelable(UNREAD_STATE))
        request.leave = it.getBoolean(LEAVE)
        val from = it.getString(FROM, Utils.EMPTY)
        if (it.getBoolean(PULL_TO_REF_FLAG) && !TextUtils.isEmpty(from)) {
            request.from = from
        }
        val to = it.getString(TO, Utils.EMPTY)
        if (it.getBoolean(HISTORY_LOAD_FLAG) && !TextUtils.isEmpty(to)) {
            request.to = to
        }
        return@let request
    }

}