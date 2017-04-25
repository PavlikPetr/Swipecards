package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос дефолтной карты пользователя
 * Created by ppavlik on 16.03.17.
 */
class PaymentNinjaGetCardRequest(context: Context) : ApiRequest(context) {

    override fun getServiceName() = "paymentNinja.getCard"

    override fun getRequestData() = JSONObject()
}

