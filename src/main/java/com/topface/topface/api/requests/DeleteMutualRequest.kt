package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.Completed
import com.topface.topface.api.stringListToJsonArray
import java.util.*

class DeleteMutualRequest(val userIds: ArrayList<String>) : BaseScruffyRequest<Completed>() {

    companion object {
        const val REQUEST_METHOD_NAME = "mutual.delete"
    }

    override fun getMethod() = REQUEST_METHOD_NAME

    override fun createJson(json: JsonObject) = with(json) {
        add("userIds", userIds.stringListToJsonArray())
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}