package com.topface.topface.ui.fragments.feed.enhanced.base

/**
 * Интерфейс жизненного цикла view model
 * Created by tiberal on 20.04.17.
 */
interface IViewModelLifeCycle {
    /**
     * Отцепить view model от вьюхи. Тут нужно убивать любые ссылки на вьюху.
     */
    fun unbind() {
    }

    /**
     * Освободить ресурсы view model. View model больше не нужна, остановить все запросы
     * и освободить все, что освобождается
     */
    fun release() {
    }
}