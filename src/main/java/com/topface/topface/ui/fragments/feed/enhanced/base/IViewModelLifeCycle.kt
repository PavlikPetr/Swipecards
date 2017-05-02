package com.topface.topface.ui.fragments.feed.enhanced.base

import android.databinding.ViewDataBinding
import android.os.Bundle

/**
 * Интерфейс жизненного цикла view model
 * Created by tiberal on 20.04.17.
 */
interface IViewModelLifeCycle {

    var args: Bundle?
    /**
     * Отцепить view model от вьюхи. Тут нужно убивать любые ссылки на вьюху.
     */
    fun bind() {

    }

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


fun <T : ViewDataBinding, VM : BaseViewModel> T.setViewModel(id: Int, viewModel: VM, args: Bundle) {
    setVariable(id, viewModel)
    viewModel.args = args
    viewModel.bind()
}