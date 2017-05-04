package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.ui.fragments.feed.app_day.AppDay

class AppDayRequest(private val mTypeFeedFragment: String) : BaseScruffyRequest<AppDay>() {

    override fun getMethod() = "ad.appListOfTheDay"

    override fun createJson(json: JsonObject) = with(json) {
        addProperty("type", mTypeFeedFragment)
    }

    override fun getResponseClass() = AppDay::class.java
}