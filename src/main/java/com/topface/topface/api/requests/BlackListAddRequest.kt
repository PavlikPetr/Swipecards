package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.intListToJsonArray
import com.topface.topface.api.responses.Completed

class BlackListAddRequest(val userIds: List<Int>) : BaseScruffyRequest<Completed>() {

    override fun getMethod() = "blacklist.add"

    override fun createJson(json: JsonObject) = with(json) {
        add("userIds", userIds.intListToJsonArray())
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}