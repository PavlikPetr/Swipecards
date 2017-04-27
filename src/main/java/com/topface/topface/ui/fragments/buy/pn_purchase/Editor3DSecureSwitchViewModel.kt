package com.topface.topface.ui.fragments.buy.pn_purchase

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.edit.EditSwitcherViewModel
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Вью-модель переключателя для создания тестовой карты с 3ds
 * Created by ppavlik on 27.04.17.
 */
class Editor3DSecureSwitchViewModel(private var mIsSelected: Boolean) {

    private var mCheckedSubscription: Subscription? = null

    val viewModel by lazy {
        EditSwitcherViewModel(isCheckedDefault = mIsSelected, textDefault = R.string.editor_3ds_switch.getString()).apply {
            setProgressVisible(false)
        }
    }

    init {
        mCheckedSubscription = viewModel.isChecked.filedObservable.subscribe(shortSubscription {
            it?.let { App.getAppComponent().eventBus().setData(ThreeDSecurePurchaseSwitch(it)) }
        })
    }

    fun release() {
        mCheckedSubscription.safeUnsubscribe()
        viewModel.release()
    }
}