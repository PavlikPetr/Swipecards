package com.topface.topface.api.requests

import android.support.annotation.IntDef
import com.google.gson.JsonObject
import com.topface.topface.api.responses.LikeSendResponse

class LikeSendRequest(val userId: Int, @LikeSendPlaces val place: Long = UNDEFINED) : BaseScruffyRequest<LikeSendResponse>() {
    companion object {
        const val UNDEFINED = -1L
        const val FROM_SEARCH = 0L
        const val FROM_PROFILE = 1L
        const val FROM_FEED = 2L

        @IntDef(FROM_SEARCH, FROM_PROFILE, FROM_FEED)
        annotation class LikeSendPlaces
    }

    override fun getMethod() = "like.send"

    override fun createJson(json: JsonObject) = with(json) {
        addProperty("userid", userId)
        if (place != UNDEFINED) {
            addProperty("place", place)
        }
    }

    override fun getResponseClass(): Class<LikeSendResponse> = LikeSendResponse::class.java
}