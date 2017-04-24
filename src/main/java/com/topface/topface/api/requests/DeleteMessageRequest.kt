package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.Completed
import com.topface.topface.api.stringListToJsonArray

class DeleteMessageRequest(private val mId: String) : BaseScruffyRequest<Completed>() {

    override fun getMethod() = "message.delete"

    override fun createJson(json: JsonObject) = with(json) {
        add("userIds", listOf<String>(mId).stringListToJsonArray())
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}