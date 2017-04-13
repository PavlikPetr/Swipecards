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
 * @param external @Deprecated {Boolean} Открывать ли URL во внешнем приложении или в модальном окне внутри ТФ.
 * @param showType {Int} - тип приложения определяющий действие по тапу
 * (1 - WEBVIEW , 2 - BROWSER, 3 - PRODUCT ), для 3 должен быть открыт экран покупки продукта
 * @param sku {String} - идентификатор продукта, поле не пустое если showType == 3.
 * Стоит обезопасится и предусмотреть случай когда в списке продуктов будет отсутствовать указанный продукт,
 * при правильных настройках такого быть не должно, но все же.
 */
data class AppDayImage(val id: String, val imgSrc: String, val url: String, val external: Boolean, val showType: Int, val sku: String)