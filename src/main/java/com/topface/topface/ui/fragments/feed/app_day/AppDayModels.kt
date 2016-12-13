package com.topface.topface.ui.fragments.feed.app_day

/**
 * Модели для "ad.appListOfTheDay"
 * Created by siberia87 on 06.10.16.
 */

/**
 * @param  firstPosition {Number} первая позиция ячейки с приложениями дня в фиде
 * @param  repeat {Number} через сколько ячеек в фиде необходимо повторять ячейку с приложениями дня
 * @param  maxCount {Number} сколько раз максимально необходимо повторять ячейку с приложением дня
 * @param  list {Array} массив объектов приложений дня
 */
data class AppDay(val firstPosition: Int, val repeat: Int, val maxCount: Int, val list: List<AppDayImage>?)

/**
 * @param id {Number} уникальный идентификатор рекламы
 * @param imgSrc {String} ссылка на картинку/банер
 * @param url {String} ссылка
 * @param external {Boolean} Открывать ли URL во внешнем приложении или в модальном окне внутри ТФ.
 */
data class AppDayImage(val id: String, val imgSrc: String, val url: String, val external: Boolean)