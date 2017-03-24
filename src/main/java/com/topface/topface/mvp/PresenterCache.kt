package com.topface.topface.mvp

import android.support.v4.util.SimpleArrayMap

/**
 * Cache of presenters
 */
class PresenterCache {
    private val mPresenters = SimpleArrayMap<String, IPresenter>()

    @Suppress("UNCHECKED_CAST")
    fun <T : IPresenter> getPresenter(clazz: String, factory: IPresenterFactory<T>): T {
        var presenter = mPresenters[clazz] as? T

        if (presenter == null) {
            presenter = factory.createPresenter()
            mPresenters.put(clazz, presenter)
        }
        return presenter
    }

    fun removePresenter(clazz: String) = mPresenters.remove(clazz)?.release()
}