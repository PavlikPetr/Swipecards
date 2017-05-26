package com.topface.topface.ui.fragments.buy.pn_purchase

import com.topface.topface.R
import com.topface.topface.api.IApi
import com.topface.topface.ui.edit.EditSwitcherViewModel
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Вью-модель для переключателя режима невидимки
 * Created by ppavlik on 26.05.17.
 */

class IvisibleModeViewModel(private var mIsSelected: Boolean, private var mApi: IApi) {

    private var mCheckedSubscription: Subscription? = null
    private var mSetProfileRequestSubscription: Subscription? = null

    val viewModel by lazy {
        EditSwitcherViewModel(isCheckedDefault = mIsSelected, textDefault = R.string.vip_invis.getString(),
                rootPaddingLeft = R.dimen.item_side_padding.getDimen().toInt(),
                rootPaddingRight = R.dimen.item_side_padding.getDimen().toInt(),
                rootPaddingTop = R.dimen.item_side_padding.getDimen().toInt(),
                rootPaddingBottom = R.dimen.item_side_padding.getDimen().toInt())
                .apply {
                    setViewVisible(true)
                }
    }

    init {
        mCheckedSubscription = viewModel.isChecked.filedObservable.subscribe(shortSubscription { state ->
            viewModel.setProgressVisible(true)
            mSetProfileRequestSubscription = mApi.callSetProfile(invisible = state).subscribe({
                // успешно засетили состояние режима невидимки
            }, {
                viewModel.isChecked.set(!state)
            }, {
                viewModel.setProgressVisible(false)
            })
        })
    }

    fun release() {
        arrayOf(mCheckedSubscription, mSetProfileRequestSubscription).safeUnsubscribe()
    }
}