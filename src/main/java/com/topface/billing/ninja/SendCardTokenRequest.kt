package com.topface.billing.ninja

import android.content.Context
import com.topface.topface.requests.ApiRequest
import org.json.JSONObject

/**
 * Sends card token received from Payment Ninja to own server
 * Created by m.bayutin on 03.03.17.
 */
internal class SendCardTokenRequest(context: Context, val data: SendCardTokenModel) : ApiRequest(context) {
    override fun getServiceName() = SERVICE_NAME

    override fun getRequestData() = JSONObject().apply {
        put(KEY_EMAIL, data.email)
        put(KEY_TOKEN, data.token)
    }

    companion object {
        const val SERVICE_NAME = "paymentNinja.addCard"
        const val KEY_TOKEN = "token"
        const val KEY_EMAIL = "email"
    }
}
