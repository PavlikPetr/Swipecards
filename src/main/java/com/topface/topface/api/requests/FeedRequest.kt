package com.topface.topface.api.requests

import com.google.gson.JsonObject
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
        if (to != null) {
            addProperty("to", to)
        }
        if (from != null) {
            addProperty("from", from)
        }
        if (previousUnreadState != null) {
            addProperty("fromUnread", previousUnreadState.from)
            addProperty("toUnread", previousUnreadState.to)
        }
        //todo запилить синглтоном не дело каждый раз инстансы создавать
        addProperty("limit", FeedLoadController().itemsLimitByConnectionType)
    }
}