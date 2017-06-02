package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.App
import com.topface.topface.api.UnreadStatePair
import com.topface.topface.utils.loadcontollers.FeedLoadController

class FeedRequest<T : Any>(val service: String, val previousUnreadState: UnreadStatePair? = null,
                           val leave: Boolean = false, val respClass: Class<T>,
                           val unread: Boolean = false, var from: String? = null,
                           var to: String? = null) : BaseScruffyRequest<T>() {
    override fun getMethod() = service
    override fun getResponseClass() = respClass

    override fun createJson(json: JsonObject) = with(json) {
        addProperty("leave", leave)
        to?.let {
            addProperty("to", it)
        }
        from?.let {
            addProperty("from", it)
        }
        previousUnreadState?.let {
            addProperty("fromUnread", previousUnreadState.from)
            addProperty("toUnread", previousUnreadState.to)
        }
        addProperty("limit", App.getAppComponent().feedLoadController().itemsLimitByConnectionType)
    }
}