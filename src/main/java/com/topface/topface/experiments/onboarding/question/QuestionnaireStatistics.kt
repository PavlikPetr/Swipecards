package com.topface.topface.experiments.onboarding.question

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow

/**
 * Методы статистики опросника
 * Created by ppavlik on 05.04.17.
 */

@GenerateStatistics
object QuestionnaireStatistics {

    /**
     * Показ очередного вопроса в рамках опросника
     * отправляем со срезом int - номер вопроса (нумерация с 1)
     * Событие с одинаковым номером вопроса может быть отправлено несколько раз (кол-во отправок в день не ограничено).
     * Такое возможно если пользователь увидел вопрос -> закрыл приложение -> запустил приложение
     */
    @SendNow(single = false, withSlices = true)
    const val QUESTION_SHOW = "mobile_question_show"

    /**
     * Показ результирующего экрана после ответа на последний вопрос
     * Событие может быть отправлено несколько раз от одного и того же пользователя (кол-во отправок в день не ограничено).
     * Такое возможно если пользователь увидел экран -> закрыл приложение -> запустил приложение
     */
    @SendNow(single = false)
    const val QUESTIONNAIRE_RESULT_SHOW = "mobile_questionnaire_result_show"

    /**
     * Показ экрана приглашения в приложение через ФБ для юзеров, которые попали под эксперимент опросника
     * Событие может быть отправлено несколько раз от одного и того же пользователя (кол-во отправок в день не ограничено).
     * Такое возможно если пользователь увидел экран -> закрыл приложение -> запустил приложение
     */
    @SendNow(single = false)
    const val FB_INVITATION_SHOW = "mobile_fb_invitation_show"

}