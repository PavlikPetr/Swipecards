package com.topface.topface.ui.fragments.feed.enhanced.utils

import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.enhanced.base.IViewModelLifeCycle

/**
 * Фрагмент для управления жизненным циклом view model
 * Created by tiberal on 20.04.17.
 */
abstract class DaggerFragment : BaseFragment() {

    /**
     * Интерфейс жизненного цикла view model прицепленной к этому фрагменту
     */
    private var mViewModelLifeCycle: IViewModelLifeCycle? = null

    /**
     * onSaveInstanceStateWasCalled - флаг того, что был вызван onSaveInstanceState, следовательно
     * мы или пересоздаем фрагмент либо свернули приложение
     */
    private var onSaveInstanceStateWasCalled = false
    /**
     * onStartAfterSavedStateWasCalled - флаг того, что был дернут onStart после onSaveInstanceState
     * значит мы развернули приложение после сворачивания
     */
    private var onStartAfterSavedStateWasCalled = false

    override fun onStart() {
        super.onStart()
        if (onSaveInstanceStateWasCalled) {
            onStartAfterSavedStateWasCalled = true
        }
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mViewModelLifeCycle = getViewModel()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    abstract fun getViewModel(): IViewModelLifeCycle

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (onSaveInstanceStateWasCalled) {
            /**
             * Если onSaveInstanceState вызвался и флаг onSaveInstanceStateWasCalled уже в true
             * значит фрагмент пересоздается после того, как мы развернули приложение и
             * onStartAfterSavedStateWasCalled нужно скинуть чтоб ViewModel не померла
             */
            onStartAfterSavedStateWasCalled = false
        } else {
            /**
             * Пересоздаем фрагмент ViewModel убивать не нужно
             */
            onSaveInstanceStateWasCalled = true
        }
    }

    override fun onDetach() {
        /**
         * Рутовый фрагмент с табами, костыльный чуть более чем полностью. При повороте создается
         * и уничтоается лишний инстанс фрагмента. По этому терминальный ивент для компонента
         * даггера кидаем только для видимого фрагмента.
         * Вообщем это адуха ребята
         * Почитай описание используемых флажков выше
         * 1) onSaveInstanceStateWasCalled && onStartAfterSavedStateWasCalled - было сохранение в стейт
         * и был вызов onStart после сохранения в стейт. Следовательно приложение было свернуто и развернуто
         * 2) !onSaveInstanceStateWasCalled - сохранения в стейт не было, следовательно мы уходи из фрагмента
         */
        if (isAdded && ((onSaveInstanceStateWasCalled && onStartAfterSavedStateWasCalled) || !onSaveInstanceStateWasCalled)) {
            terminateImmortalComponent()
            mViewModelLifeCycle?.release()
        }
        super.onDetach()
        mViewModelLifeCycle?.unbind()
    }

    protected open fun terminateImmortalComponent() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModelLifeCycle?.release()
    }

}
