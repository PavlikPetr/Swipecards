package com.topface.topface.data

import com.topface.topface.utils.Utils

/**
 * Модельки для запроса опций, будем постепенно переводить на котлин
 * Created by ppavlik on 22.12.16.
 */
data class FBInviteSettings(var enabled: Boolean = false, var appLink: String = Utils.EMPTY, var iconUrl: String = Utils.EMPTY) {
    fun isEmpty() = this == FBInviteSettings()
}

/**
 * Модель для запроса опций нового варианта попапа оценки приложения
 */
data class RatePopupNewVersion(var enabled: Boolean = false, var notNowTimeout: Int = 0, var badRateTimeout: Int = 0) {
    fun isEmpty() = this == RatePopupNewVersion()
}

/**
 * Модель объекта настроек для редизайна симпатий
 *
 * Оставляю в виде объекта, чтобы не ломать серилизацию/десерилизацию, а также в будущем может быть введена блокировка,
 * настройки которой будут здесь же
 */
data class SerialLikesSettings(val enabled: Boolean = false)