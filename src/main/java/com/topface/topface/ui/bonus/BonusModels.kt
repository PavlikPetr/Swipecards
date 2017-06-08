package com.topface.topface.ui.bonus

/**
 * Created by ppavlik on 02.06.17.
 * Модельки для "разводящего экрана" оферволов
 */

/**
 * Загрузчик на весь экран, вместо кнопок
 */
data class Loader(private val mDiffTemp: Int = 0)

/**
 * Итем кнопки
 *
 * @param imgRes - ресурс картинки для кнопки
 * @param text - текст на кнопке
 * @param type - тип офервола ("sympathy"/"coins"/"vip")
 */
data class OfferwallButton(val imgRes: Int, val text: String, val type: String)