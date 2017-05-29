package com.topface.topface.api.requests

import android.location.Location
import com.google.gson.JsonObject
import com.topface.topface.api.responses.Completed
import com.topface.topface.utils.Utils

/**
 * Запрос на изменение данных профиля пользователя
 *
 * @param name - новое имя пользователя в UTF-8
 * @param age - возраст пользователя. Минимальное значение - 12, максимальное - 99.
 *              Если указано меньше минимального или болше максимального значение кропятся по ОДЗ
 * @param sex - новый пол пользователя
 * @param location - координаты пользователя
 * @param cityid - идентификатор города пользователя
 * @param status - статус
 * @param background - background для випа (старое говно, давно не используется, в 8-й версии апи должно быть выпилено сервером)
 * @param invisible - режим невидимки
 * @param xstatus - цель знакомства
 * @param isAutoReplyAllowed - настройка автоотправки сообщений
 */
class SettingsRequest(private val name: String, private val age: Int, private val sex: Int,
                      private val location: Location?, private val cityid: Int, private val status: String,
                      private val background: Int, private val invisible: Boolean?, private val xstatus: Int,
                      private val isAutoReplyAllowed: Boolean?) : BaseScruffyRequest<Completed>() {

    companion object {
        const val REQUEST_METHOD_NAME = "user.setProfile"
    }

    override fun getMethod() = REQUEST_METHOD_NAME

    override fun createJson(json: JsonObject) {
        if (!name.isNullOrEmpty()) {
            json.addProperty("name", name)
        }
        if (!status.isNullOrEmpty()) {
            json.addProperty("status", status)
        }
        if (age != -1) {
            json.addProperty("age", age)
        }
        if (sex != -1) {
            json.addProperty("sex", sex)
        }
        location?.let {
            json.addProperty("lat", location.latitude)
            json.addProperty("lng", location.longitude)
        }
        if (cityid != -1) {
            json.addProperty("cityId", cityid)
        }
        if (background != -1) {
            json.addProperty("background", background)
        }
        invisible?.let { json.addProperty("invisible", invisible) }
        if (xstatus != -1) {
            json.addProperty("xstatus", xstatus)
        }
        isAutoReplyAllowed?.let { json.addProperty("allowAutoreply", isAutoReplyAllowed) }
    }

    override fun getResponseClass(): Class<Completed> = Completed::class.java
}