package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.content.Context
import com.topface.topface.requests.ApiRequest
import org.json.JSONObject

class QuestionnaireSearchRequest(context: Context, val methodName: String, val data: JSONObject) : ApiRequest(context) {
    override fun getServiceName() = methodName

    override fun getRequestData() = data
}