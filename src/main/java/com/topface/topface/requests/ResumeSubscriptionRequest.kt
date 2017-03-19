package com.topface.topface.requests

import android.content.Context
import com.topface.framework.utils.Debug
import org.json.JSONArray
import org.json.JSONObject

/**
 * Запрос на восстановление подписки
 * Created by ppavlik on 16.03.17.
 */
class ResumeSubscriptionRequest(context: Context) : ApiRequest(context) {

    override fun getServiceName() = "paymentNinja.resumeSubscription"

    override fun getRequestData() = JSONObject()
}

