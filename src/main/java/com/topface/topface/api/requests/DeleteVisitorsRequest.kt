package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.Completed
import com.topface.topface.api.stringListToJsonArray
import java.util.*

class DeleteVisitorsRequest(val userIds: ArrayList<String>) : BaseScruffyRequest<Completed>() {

    override fun getMethod() = "visitor.delete"

    override fun createJson(json: JsonObject) = with(json) {
        add("ids", userIds.stringListToJsonArray())
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}