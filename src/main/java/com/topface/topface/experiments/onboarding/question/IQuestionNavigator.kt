package com.topface.topface.experiments.onboarding.question

import android.support.v4.app.Fragment

/**
 * Интерфейс для показа экранов опросника
 * Created by petrp on 29.03.2017.
 */

interface IQuestionNavigator {
    fun addQuestionScreen(fragment: Fragment?)

    fun showResultScreen()
}
