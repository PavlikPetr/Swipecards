package com.topface.topface.mvp

/**
 * Factory which creates presenters
 */
interface IPresenterFactory<out T: IPresenter> {
    fun createPresenter(): T
}