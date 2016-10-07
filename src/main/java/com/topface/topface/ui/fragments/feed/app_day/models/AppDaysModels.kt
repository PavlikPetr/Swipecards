package com.topface.topface.ui.fragments.feed.app_day.models

/**
 * Модель банера апы дня и картинки
 * Created by siberia87 on 06.10.16.
 */

data class AppDay (val firstPosition: Int, val repeat: Int, val maxCount: Int, val result: List<AppDayImage>)
data class AppDayImage(val imgSrc: String, val url: String, val external: Boolean)