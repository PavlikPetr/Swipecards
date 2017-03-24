package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос на Добавление карты
 * Created by ppavlik on 16.03.17.
 */
class PaymentNinjaPurchaseRequest(context: Context, val productId: String, val source: String) : ApiRequest(context) {

    override fun getServiceName() = "paymentNinja.purchase"

    override fun getRequestData() = JSONObject().apply {
        put("productId", productId)
        put("source", source)
    }
}

