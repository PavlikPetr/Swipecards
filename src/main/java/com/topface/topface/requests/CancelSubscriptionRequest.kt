package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос на отмену подписки
 * Created by ppavlik on 16.03.17.
 */
class CancelSubscriptionRequest(context: Context, val subscriptionType: String) : ApiRequest(context) {

    override fun getServiceName() = "paymentNinja.cancelSubscription"

    override fun getRequestData() = JSONObject().apply { put("type", subscriptionType) }
}

