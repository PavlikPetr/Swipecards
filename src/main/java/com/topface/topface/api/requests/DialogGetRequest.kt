package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.History
import com.topface.topface.utils.loadcontollers.FeedLoadController

class DialogGetRequest(private val userId: Int, private var from: String?,
                       private var to: String?, private val leave: Boolean = false) :
        BaseScruffyRequest<History>() {

    override fun getMethod() = "dialog.get"

    override fun getResponseClass() = History::class.java

    override fun createJson(json: JsonObject) = with(json) {
        to?.let {
            addProperty("to", it)
        }
        from?.let {
            addProperty("from", it)
        }
        addProperty("userId", userId)
        addProperty("leave", leave)
        //todo запилить синглтоном не дело каждый раз инстансы создавать
        addProperty("limit", FeedLoadController().itemsLimitByConnectionType)
    }
}