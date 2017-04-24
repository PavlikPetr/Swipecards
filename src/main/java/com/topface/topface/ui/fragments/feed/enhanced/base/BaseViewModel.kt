package com.topface.topface.ui.fragments.feed.enhanced.base

import android.content.Intent
import android.os.Bundle
import com.topface.topface.utils.ILifeCycle

open class BaseViewModel : IViewModelLifeCycle, ILifeCycle {

    override fun unbind() {
    }

    override fun release() {
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