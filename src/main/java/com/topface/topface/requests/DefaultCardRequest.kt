package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос дефолтной карты пользователя
 * Created by ppavlik on 16.03.17.
 */
class DefaultCardRequest(context: Context) : ApiRequest(context) {

    override fun getServiceName() = "paymentNinja.getDefaultCard"

    override fun getRequestData() = JSONObject()
}

