package com.topface.topface.experiments.onboarding.question.single_list

import com.topface.framework.utils.Debug
import com.topface.topface.experiments.onboarding.question.QuestionSingleChoiceButton
import com.topface.topface.experiments.onboarding.question.QuestionType3
import com.topface.topface.utils.databinding.SingleObservableArrayList

/**
 * VM for question with list of answers and single choice
 */
class QSingleListFragmentViewModel {
    val data = SingleObservableArrayList<Any>()


    init {
        val model = QuestionType3("type3FieldName", listOf(
                QuestionSingleChoiceButton("M", "0"),
                QuestionSingleChoiceButton("W", "1")
        ))
        data.addAll(model.buttons)
        Debug.log("--- now has ${data.observableList.size}")
    }
}