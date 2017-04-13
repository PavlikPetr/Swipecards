package com.topface.topface.ui.fragments.feed.enhanced.base

import android.content.Intent
import android.os.Bundle
import com.topface.topface.utils.ILifeCycle

open class BaseViewModel : ILifeCycle {

    /**
     * Отцепить view model от вьюхи. Тут нужно убивать любые ссылки на вьюху.
     */
    open fun unbind() {
    }

    /**
     * Освободить ресурсы view model. View model больше не нужна, остановить все запросы
     * и освободить все, что освобождается
     */
    open fun release() {
    }

    /**
     * Чтобы работали методы представленные ниже, нужно зарегестрировать модель в NavigationActivity
     * для делегирования ивентов
     */
    override fun onSavedInstanceState(state: Bundle) {
    }

    override fun onRestoreInstanceState(state: Bundle) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun onResume() {
    }

    override fun onPause() {
    }
}