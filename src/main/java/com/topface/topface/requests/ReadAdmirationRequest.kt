package com.topface.topface.requests

import android.content.Context
import com.topface.topface.requests.handlers.ErrorCodes
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by ppavlik on 11.10.16.
 * запрос на прочтение восхищений
 */
class ReadAdmirationRequest(context: Context, val idArray: List<Int>) : ApiRequest(context) {

    @Throws(JSONException::class)
    override fun getRequestData(): JSONObject {
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        for (id in idArray) {
            jsonArray.put(id)
        }
        jsonObject.put("feedIds", jsonArray)
        return jsonObject
    }

    override fun exec() {
        if (!isContainEmptyId()) {
            super.exec()
        } else {
            handleFail(ErrorCodes.ERRORS_PROCESSED, "Invalid id")
        }
    }

    private fun isContainEmptyId() = idArray.isEmpty() || idArray.find { it <= 0 } != null

    override fun getServiceName() = "admiration.read"
}