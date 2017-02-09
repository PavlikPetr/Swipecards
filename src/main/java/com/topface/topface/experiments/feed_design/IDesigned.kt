package com.topface.topface.experiments.feed_design

/**
 * Базовый интерфейсик для тех, у кого есть версия дизайна
 * Created by m.bayutin on 08.02.17.
 */
interface IDesigned<T> {
    fun getDesignVersion() : T
}