package com.topface.topface.experiments.onboarding.question.single_list

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.experiments.onboarding.question.Button
import com.topface.topface.experiments.onboarding.question.UserChooseAnswer
import org.json.JSONObject

class SingleListItemViewModel(val model: Button, val fieldName: String, val isEnabled: ObservableBoolean) {
    val title = ObservableField<String>(model.title)

    fun onClick() {
        isEnabled.set(false)
        if (!fieldName.isNullOrEmpty()) {
            App.getAppComponent().eventBus().setData(UserChooseAnswer(JSONObject().apply {
                    put(fieldName, model.value)
            }))
        }
    }
}