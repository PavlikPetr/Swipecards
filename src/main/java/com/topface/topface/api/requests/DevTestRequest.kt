package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.api.responses.DevTestDataResponse

class DevTestRequest(val delay: Int, val required: Int, val error: Int? = null,
                     val errorMessage: String? = null) : BaseScruffyRequest<DevTestDataResponse>() {

    override fun getResponseClass() = DevTestDataResponse::class.java

    override fun createJson(json: JsonObject) {
        json.addProperty("required", required)
        json.addProperty("error", error)
        json.addProperty("errorMessage", errorMessage)
      //  json.addProperty("delay", delay)
    }

    override fun getMethod() = "dev.test"
}