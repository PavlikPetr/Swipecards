package com.topface.topface.experiments.onboarding.question.single_list

import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.experiments.onboarding.question.QuestionSingleChoiceButton

class SingleListItemViewModel(val model: QuestionSingleChoiceButton) {
    val title = ObservableField<String>(model.title)

    fun onClick() {
        Debug.log("--- on ${title.get()} click with value = ${model.value}")
    }
}