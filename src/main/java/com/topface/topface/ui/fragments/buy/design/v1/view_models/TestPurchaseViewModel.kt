package com.topface.topface.ui.fragments.buy.design.v1.view_models

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.edit.EditSwitcherViewModel
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Вью-модель для переключателя тестовых покупок
 * Created by ppavlik on 09.06.17.
 */

class TestPurchaseViewModel(private var mIsSelected: Boolean) {

    private var mCheckedSubscription: Subscription? = null

    val viewModel by lazy {
        EditSwitcherViewModel(isCheckedDefault = mIsSelected, textDefault = R.string.editor_test_buy.getString())
                .apply {
                    setViewVisible(true)
                }
    }

    init {
        mCheckedSubscription = viewModel.isChecked.filedObservable.subscribe(shortSubscription {
            App.getUserConfig().apply {
                testPaymentFlag = it
                saveConfig()
            }
        })
    }

    fun release() {
        mCheckedSubscription.safeUnsubscribe()
    }
}