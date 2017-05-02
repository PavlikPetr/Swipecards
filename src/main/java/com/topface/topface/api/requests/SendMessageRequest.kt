package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.HistoryItem

class SendMessageRequest(private val mUserId: Int, private val mMessage: String, private val mIsInstant: Boolean) :
        BaseScruffyRequest<HistoryItem>() {

    override fun getMethod() = "message.send"

    override fun getResponseClass() = HistoryItem::class.java

    override fun createJson(json: JsonObject) = with(json) {
        addProperty("userId", mUserId)
        addProperty("message", mMessage)
        addProperty("isInstant", mIsInstant)
    }
}