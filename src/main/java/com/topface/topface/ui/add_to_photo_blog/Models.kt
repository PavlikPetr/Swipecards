package com.topface.topface.ui.add_to_photo_blog

/**
 * Models for experimental add-to-photo-blog screen
 * Created by mbayutin on 10.01.17.
 */

/**
 * Итем заголовка с аватаркой
 */
class HeaderItem()

/**
 * Итем горизонтального списка фоток пользователя
 */
data class PhotoListItem(val lastSelectedPhotoId: Int)

/**
 * Итем кнопки "Разместить" с подписью о количестве монет
 */
data class PlaceButtonItem(val price: Int)

/**
 * Событие тапа по фотке
 */
data class PhotoSelectedEvent(val id: Int)

/**
 * Событие тапа по кнопке "разместить"
 */
class PlaceButtonTapEvent()