package com.topface.topface.requests

import android.content.Context
import org.json.JSONObject

/**
 * Запрос списка продуктов для payment ninja
 * Created by ppavlik on 16.03.17.
 */
class PaymentNinjaProductsRequest(context: Context) : ApiRequest(context) {
    override fun getServiceName() = "paymentNinja.getProducts"

    override fun getRequestData() = JSONObject()
}