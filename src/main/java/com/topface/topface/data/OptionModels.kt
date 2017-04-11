package com.topface.topface.data

import com.topface.topface.utils.Utils

/**
 * Модельки для запроса опций, будем постепенно переводить на котлин
 * Created by ppavlik on 22.12.16.
 */
data class FBInviteSettings(var enabled: Boolean = false, var appLink: String = Utils.EMPTY, var iconUrl: String = Utils.EMPTY) {
    fun isEmpty() = this.equals(FBInviteSettings())
}

/**
 * Модель для запроса опций нового варианта попапа оценки приложения
 */
data class RatePopupNewVersion(var enabled: Boolean = false, var notNowTimeout: Long = 0, var badRateTimeout: Long = 0) {
    fun isEmpty() = this.equals(RatePopupNewVersion())
}