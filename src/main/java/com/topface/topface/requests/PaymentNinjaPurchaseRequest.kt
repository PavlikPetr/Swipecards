package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос на Добавление карты
 * Created by ppavlik on 16.03.17.
 */
class PaymentNinjaPurchaseRequest(context: Context, val productId: String, val place: String,
                                  val isTest: Boolean, val isAutoFillEnabled: Boolean, val isNeed3DS: Boolean) : ApiRequest(context) {

    override fun getServiceName() = "paymentNinja.purchase"

    override fun getRequestData() = JSONObject().apply {
        put("productId", productId)
        put("place", place)
        put("isTest", isTest)
        put("enableAutorefill", isAutoFillEnabled)
        put("force3ds", isNeed3DS)
    }
}

