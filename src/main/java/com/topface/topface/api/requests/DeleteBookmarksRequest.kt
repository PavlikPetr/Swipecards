package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.Completed
import com.topface.topface.api.stringListToJsonArray
import java.util.*

class DeleteBookmarksRequest(val userIds: ArrayList<String>) : BaseScruffyRequest<Completed>() {

    override fun getMethod() = "bookmark.delete"

    override fun createJson(json: JsonObject) = with(json) {
        val ids = userIds.map {
            it.substringAfter(":", it)
        }.stringListToJsonArray()
        add("userIds", ids)
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}