package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.intListToJsonArray
import com.topface.topface.api.responses.Completed

class BlackListAddRequest(val userIds: List<Int>) : BaseScruffyRequest<Completed>() {

    companion object {
        const val REQUEST_METHOD_NAME = "blacklist.add"
    }

    override fun getMethod() = REQUEST_METHOD_NAME

    override fun createJson(json: JsonObject) = with(json) {
        add("userIds", userIds.intListToJsonArray())
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}