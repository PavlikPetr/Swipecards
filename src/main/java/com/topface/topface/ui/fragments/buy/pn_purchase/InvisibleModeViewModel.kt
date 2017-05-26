package com.topface.topface.ui.fragments.buy.pn_purchase

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.IApi
import com.topface.topface.ui.edit.EditSwitcherViewModel
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Вью-модель для переключателя режима невидимки
 * Created by ppavlik on 26.05.17.
 */

class InvisibleModeViewModel(private var mApi: IApi) : BaseViewModel() {

    private var mCheckedSubscription: Subscription? = null
    private var mSetProfileRequestSubscription: Subscription? = null
    private var mInvisibleModeStateSuccessInstalled = App.get().profile.invisible

    val viewModel by lazy {
        EditSwitcherViewModel(isCheckedDefault = mInvisibleModeStateSuccessInstalled, textDefault = R.string.vip_invis.getString(),
                rootPaddingLeft = R.dimen.item_side_padding.getDimen().toInt(),
                rootPaddingRight = R.dimen.item_side_padding.getDimen().toInt(),
                rootPaddingTop = R.dimen.item_side_padding.getDimen().toInt(),
                rootPaddingBottom = R.dimen.item_side_padding.getDimen().toInt())
                .apply {
                    setViewVisible(true)
                }
    }

    init {
        mCheckedSubscription = viewModel.isChecked.filedObservable
                .filter { it != mInvisibleModeStateSuccessInstalled } // фильтр, чтобы не отправлять запрос с уже заданным состоянием
                .subscribe(shortSubscription { state ->
                    viewModel.setProgressVisible(true)
                    mSetProfileRequestSubscription = mApi.callSetProfile(invisible = state).subscribe({
                        // запоминаем последнее успешно засеченое состояние переключателя
                        mInvisibleModeStateSuccessInstalled = state
                    }, {
                        viewModel.isChecked.set(!state)
                    }, {
                        viewModel.setProgressVisible(false)
                    })
                })
    }

    override fun release() = arrayOf(mCheckedSubscription, mSetProfileRequestSubscription).safeUnsubscribe()
}