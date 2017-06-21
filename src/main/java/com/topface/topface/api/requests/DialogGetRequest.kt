package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.App
import com.topface.topface.api.responses.History
import com.topface.topface.utils.loadcontollers.FeedLoadController

class DialogGetRequest(private val userId: Int, private var from: String?,
                       private var to: String?, private val leave: Boolean = false) :
        BaseScruffyRequest<History>() {

    companion object{
        const val REQUEST_METHOD_NAME = "dialog.get"
    }

    override fun getMethod() = REQUEST_METHOD_NAME

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
        addProperty("limit", App.getAppComponent().feedLoadController().itemsLimitByConnectionType)
    }
}