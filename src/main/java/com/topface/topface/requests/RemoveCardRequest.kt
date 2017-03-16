package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос на удаление карты payment ninja
 * Created by ppavlik on 16.03.17.
 */
class RemoveCardRequest(context: Context) : ApiRequest(context) {

    override fun getServiceName() = "paymentNinja.removeCard"

    override fun getRequestData() = JSONObject()
}

