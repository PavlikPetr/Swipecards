package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.Completed

class LikeReadRequest(val senderId: Int) : BaseScruffyRequest<Completed>() {

    override fun getMethod() = "like.read"

    override fun createJson(json: JsonObject) = json.addProperty("senderId", senderId)

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}