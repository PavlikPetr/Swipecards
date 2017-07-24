package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.Completed

/**
 * запрос на разблокировку симпатий
 */
class BuyLikesAccessRequest : BaseScruffyRequest<Completed>() {

    override fun getMethod() = "like.buyAccess"

    override fun createJson(json: JsonObject) {}

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}