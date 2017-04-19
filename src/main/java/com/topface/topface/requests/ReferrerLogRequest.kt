package com.topface.topface.requests

import android.content.Context
import com.kochava.android.tracker.Feature
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData
import org.json.JSONObject

/**
 * Запрос на отправку данных об инсталах от kochava и appsflyer
 * Created by ppavlik on 15.03.17.
 */
class ReferrerLogRequest(context: Context, private val kochavaData: String? = null,
                         private val appsflyerData: String? = null,
                         private val adjustData: AdjustAttributeData? = null) : ApiRequest(context) {
    companion object {
        private const val SERVICE_NAME = "referral.log"
        private const val TRACKER_KOCHAVA = "kochavaTracker"
        private const val TRACKER_APPSFLYER = "appsflyerTracker"
        private const val TRACKER_ADJUST = "adjustTracker"
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
                adjustData?.let {
                    fillJson(TRACKER_ADJUST, JsonUtils.toJson(it).toString())
                }
            }

    override fun exec() {
        if ((kochavaData != null || appsflyerData != null || adjustData != null) && App.getAppComponent().weakStorage().isFirstSessionAfterInstall) {
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