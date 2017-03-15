package com.topface.topface.requests

import android.content.Context
import com.kochava.android.tracker.Feature
import com.topface.framework.utils.Debug
import org.json.JSONObject

/**
 * Запрос на отправку данных об инсталах от kochava и appsflyer
 * Created by ppavlik on 15.03.17.
 */
class ReferrerLogRequest(context: Context, private val kochavaData: String? = null,
                         private val appsflyerData: String? = null) : ApiRequest(context) {
    companion object {
        private const val SERVICE_NAME = "referral.log"
        private const val TRACKER_KOCHAVA = "kochavaTracker"
        private const val TRACKER_APPSFLYER = "appsflyerTracker"
    }

    override fun getServiceName() = SERVICE_NAME

    override fun getRequestData() =
            JSONObject().apply {
                kochavaData?.let {
                    fillJson(TRACKER_KOCHAVA, it)
                }
                appsflyerData?.let {
                    fillJson(TRACKER_APPSFLYER, it)
                }
            }

    override fun exec() {
        if (kochavaData != null || appsflyerData != null) {
            super.exec()
        } else {
            Debug.error("Request $SERVICE_NAME can not be sent, the data is empty")
        }
    }

    override fun isNeedAuth() = false

    private fun JSONObject.fillJson(trackerName: String, referralData: String) = apply {
        put("diviceId", Feature.getKochavaDeviceId())
        put("trackerName", trackerName)
        put("referralData", referralData)
        put("runNumber", com.topface.topface.App.getAppConfig().appStartEventNumber)
    }
}