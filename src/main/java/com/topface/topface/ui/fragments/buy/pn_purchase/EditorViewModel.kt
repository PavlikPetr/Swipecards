package com.topface.topface.ui.fragments.buy.pn_purchase

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.ui.edit.EditSwitcherViewModel
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Вью-модель для переключателя тестовых покупок
 * Created by ppavlik on 24.04.17.
 */

class EditorViewModel {

    private var mProfileSubscription: Subscription? = null

    val viewModel by lazy {
        EditSwitcherViewModel(isCheckedDefault = false, textDefault = R.string.editor_test_buy.getString())
                .apply {
                    setViewVisible(false)
                    isChecked.set(false)
                }
    }

    init {
        mProfileSubscription = App.getAppComponent().appState().getObservable(Profile::class.java)
                .map { it.isEditor }
                .distinctUntilChanged()
                .subscribe(shortSubscription {
                    it?.let { viewModel.setViewVisible(it) }
                })
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
        viewModel.release()
    }
}