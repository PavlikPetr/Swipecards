package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.intListToJsonArray
import com.topface.topface.api.responses.Completed

class DeleteMessageRequest(private val mId: Int) : BaseScruffyRequest<Completed>() {

    companion object {
        const val REQUEST_METHOD_NAME = "message.delete"
    }

    override fun getMethod() = "message.delete"

    override fun createJson(json: JsonObject) = with(json) {
        add("items", listOf(mId).intListToJsonArray())
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}