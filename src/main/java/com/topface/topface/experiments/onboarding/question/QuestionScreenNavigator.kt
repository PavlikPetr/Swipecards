package com.topface.topface.experiments.onboarding.question

import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.TextInputScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.DigitInputScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.MultiSelectScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.RangeQuestionScreen
import com.topface.topface.experiments.onboarding.question.QuestionSettings.Companion.SingleChoiseScreen
import com.topface.topface.experiments.onboarding.question.multiselectCheckboxList.MultiSelectCheckboxListFragment
import com.topface.topface.experiments.onboarding.question.digit_input.DigitInputFragment
import com.topface.topface.experiments.onboarding.question.range.QRangeFragment
import com.topface.topface.experiments.onboarding.question.single_list.QSingleListFragment
import com.topface.topface.experiments.onboarding.question.text_input.TextInputFragment

/**
 * Навигатор по опроснику
 * Created by petrp on 29.03.2017.
 */
class QuestionScreenNavigator(private val questions: Array<QuestionSettings>, private var startPosition: Int = -1,
                              private val questionNavigator: IQuestionNavigator) {
    fun show(position: Int? = null) {
        val pos = position ?: startPosition + 1
        startPosition = pos
        questions.getOrNull(pos)?.let { questionNavigator.addQuestionScreen(getFragmentByType(it)) } ?: questionNavigator.showResultScreen()
    }

    fun getCurrentPosition() = startPosition

    fun getTotalPOsition() = questions.size

    private fun getFragmentByType(data: QuestionSettings) =
            when (data.type) {
                RangeQuestionScreen -> QRangeFragment.newInstance(data.typeFirst)
                SingleChoiseScreen -> QSingleListFragment.newInstance(data.typeSecond)
                DigitInputScreen -> DigitInputFragment.newInstance(data.typeThird)
                MultiSelectScreen -> MultiSelectCheckboxListFragment.newInstance(data.typeFourth)
                TextInputScreen -> TextInputFragment.newInstance(data.typeFifth)
                else -> null
            }
}