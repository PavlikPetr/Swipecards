package com.topface.topface.statistics

import com.topface.statistics.processor.annotations.GenerateStatistics
import com.topface.statistics.processor.annotations.SendNow
import com.topface.topface.App

/**
 * Ключи для отправки статистики приглашений в приложение через FB
 * Created by ppavlik on 26.12.16.
 */

@GenerateStatistics
object FBInvitesStatistics {

    /**
     * Клик по кнопке приглашения друзей через FB
     */
    @SendNow(single = false)
    const val FB_INVITE_BUTTON_CLICK = "mobile_fb_invite_button_click"

    /**
     * Показ диалога приглашений друзей через FB
     */
    @SendNow(single = false)
    const val FB_INVITE_SHOW = "mobile_fb_invite_show"

    /**
     * Уникальный клик по кнопке приглашения друзей через FB
     */
    @SendNow(single = false, unique = true)
    const val FB_INVITE_BUTTON_CLICK_UNIQUE = "mobile_fb_invite_button_click_unique"

    /**
     * Уникальный показ диалога приглашений друзей через FB
     */
    @SendNow(single = false, unique = true)
    const val FB_INVITE_SHOW_UNIQUE = "mobile_fb_invite_show_unique"

    /**
     * Авторизация пользователя, который получил инвайт из FB
     */
    @SendNow(single = false, withSlices = true)
    const val FB_INVITE_AUTHORIZE = "mobile_fb_invite_authorize"

    /**
     * Регистрация пользователя, который получил инвайт из FB
     */
    @SendNow(single = false, withSlices = true)
    const val FB_INVITE_REGISTER = "mobile_fb_invite_register"

}