package com.topface.topface.api.requests

import com.google.gson.JsonObject
import com.topface.topface.App
import com.topface.topface.data.AdsSettings

/**
 * Created by ppavlik on 31.05.17.
 * Возвращает информацию о текущем обычном баннере и настройках показа
 *
 * Параметры запроса
 * Обязательные
 * {String} startNumber - порядковый номер запуска приложения за календарные сутки, нумерация с 1
 *
 * Параметры ответа
 * {Object} banner - информация о баннере для показа
 * {String} type - тип баннера: SDK, IMG или WEB
 * {String} name - наименование баннера
 * {String} url - строка URL отображения изображения баннера (ОПЦИОНАЛЬНО)
 * {String} action - идентификатор возможных действий с баннером (ОПЦИОНАЛЬНО)
 * {String} parameter - строка значения параметра действия (ОПЦИОНАЛЬНО)
 * {String} adAppId - идентификатор приложения SDK (ОПЦИОНАЛЬНО, только для типов баннеров SDK) ~ #50490
 * {Number} nextRequestNoEarlierThen - минимальный интервал между показами стартового фул-скрин баннера в секундах
 */
class BannerSettingsRequest(private val startNumber: Long) : BaseScruffyRequest<AdsSettings>() {

    override fun getMethod() = "banner.getCommon"

    override fun createJson(json: JsonObject) = with(json) {
        addProperty("startNumber", startNumber)
    }

    override fun getResponseClass() = AdsSettings::class.java
}