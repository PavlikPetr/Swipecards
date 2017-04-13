package com.topface.topface.ui.views.image_switcher

// Класс для того, чтобы уведомить подписчиков о клике по картинке
class ImageClick

// Сообщаем о необходимости дозагрузить линки на фото
data class PreloadPhoto(val position:Int)