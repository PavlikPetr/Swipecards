package com.topface.topface.data

import com.topface.topface.utils.Utils

/**
 * Модельки для запроса опций, будем постепенно переводить на котлин
 * Created by ppavlik on 22.12.16.
 */
data class FBInviteSettings(var enabled: Boolean = false, var appLink: String = Utils.EMPTY, var iconUrl: String = Utils.EMPTY) {
    fun isEmpty() = this.equals(FBInviteSettings())
}