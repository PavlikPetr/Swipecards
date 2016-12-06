package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос на чтение восхищений
 * Created by tiberal on 06.12.16.
 */
class MutualReadRequest(context: Context, private val mIdArray: List<Int>) : ApiRequest(context) {

    constructor(context: Context, mIdArray: Int) : this(context, listOf(mIdArray))

    override fun getServiceName() = "mutual.read"

    override fun getRequestData() = JSONObject().apply {
        val jsonArray = JSONArray().apply {
            for (id in mIdArray) {
                put(id)
            }
        }
        put("ids", jsonArray)
    }

    override fun exec() = if (mIdArray.isNotEmpty()) {
        super.exec()
    } else {
        Debug.log("MutualReadRequest idArray is empty")
    }
}

