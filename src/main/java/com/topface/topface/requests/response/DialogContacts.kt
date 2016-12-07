package com.topface.topface.requests.response

import com.topface.topface.data.FeedUser

/**
 * Модели для "mutualBand.getList"
 * Created by tiberal on 04.12.16.
 */

/**
 * @param  counter {Number}счетчик непрочитанных элементов ленты
 * @param  more {Boolean}признак наличие большего количества взаимных симпатий
 * @param  items {Array}массив экземпляров элементов ленты
 */
data class DialogContacts(var counter: Byte = 0, var more: Boolean = false, val items: MutableList<DialogContactsItem> = mutableListOf())

/**
 * @param type {Number} идентификатор типа элемента ленты
 * @param id {Number} идентификатор элемента ленты
 * @param created {Number} таймстамп времени создания элемента ленты
 * @param target {Number} идентификатор направления элемента ленты
 * @param unread {Boolean} показатель, является ли элемент ленты непрочитанным
 * @param user {Object} экземпляр собеседника. Начиная с 6 версии метод History не содержит это поле.
 * @param highrate {Boolean} признак восхищения (это то, которое платное)
 */
data class DialogContactsItem(val type: Int, val id: Int, val created: Long, val target: Int, val unread: Boolean, val highrate: Boolean, val user: FeedUser)
