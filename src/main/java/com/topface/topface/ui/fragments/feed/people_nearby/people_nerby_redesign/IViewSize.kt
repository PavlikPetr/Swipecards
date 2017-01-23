package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

/**
 * Интерфейс для проброса размера вьюхи
 * Created by petrp on 18.01.2017.
 */
interface IViewSize {
    fun size(size: Size)
}

// моделька размера вью
data class Size(var height: Int, var width: Int)