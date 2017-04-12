package com.topface.topface.experiments.onboarding.question

import android.content.Context
import com.topface.topface.requests.ApiRequest
import org.json.JSONObject

/**
 * Запрос на получение настроек для эксперимента с опросником
 * Created by ppavlik on 31.03.17.
 */
class QuestionnaireGetListRequest(context: Context, private val locale: String) : ApiRequest(context) {
    companion object {
        private const val SERVICE_NAME = "questionnaire.getList"
        private const val LOCALE = "locale"
    }

    override fun getServiceName() = SERVICE_NAME

    override fun getRequestData() =
            JSONObject().apply {
                put(LOCALE, locale)
            }

    override fun isNeedAuth() = false
}