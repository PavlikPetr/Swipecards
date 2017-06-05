package com.topface.topface.ui.bonus.models

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.bonus.Loader
import com.topface.topface.ui.bonus.OfferwallButton
import com.topface.topface.ui.external_libs.ironSource.IronSourceManager
import com.topface.topface.ui.external_libs.ironSource.IronSourceOfferwallEvent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Created by ppavlik on 02.06.17.
 * Вью модель для "разводящего" экрана оферволов
 * Экран содержит 1-3, каждая из которых отвечает за категорию благ, который пользователь получиит
 * ри выполнении офервола
 */
class BonusViewModel : BaseViewModel() {
    val data = MultiObservableArrayList<Any>()

    private val mIronSourceManager by lazy {
        App.getAppComponent().ironSourceManager()
    }

    private var mOfferwallState: Subscription? = null

    init {
        showButtons()
        mOfferwallState = mIronSourceManager.offerwallObservable
                .filter {
                    it.type == IronSourceOfferwallEvent.CALL_OFFERWALL ||
                            it.type == IronSourceOfferwallEvent.OFFERWALL_CLOSED ||
                            it.type == IronSourceOfferwallEvent.OFFERWALL_OPENED
                }
                .applySchedulers()
                .subscribe(shortSubscription {
                    when (it.type) {
                        IronSourceOfferwallEvent.CALL_OFFERWALL -> showLoader()
                        IronSourceOfferwallEvent.OFFERWALL_CLOSED, IronSourceOfferwallEvent.OFFERWALL_OPENED -> showButtons()
                    }
                })
    }

    override fun bind() {

    }

    private fun showButtons() {
        val items = arrayListOf<Any>()
        App.get().options.offerwallWithPlaces.getLeftMenu().forEach {
            when (it) {
                IronSourceManager.SYMPATHIES_OFFERWALL -> OfferwallButton(R.drawable.offer_sympaties, R.string.offers_free_likes.getString(), it)
                IronSourceManager.COINS_OFFERWALL -> OfferwallButton(R.drawable.offer_coins, R.string.offers_free_coins.getString(), it)
                IronSourceManager.VIP_OFFERWALL -> OfferwallButton(R.drawable.offer_vip, R.string.offers_free_vip.getString(), it)
                else -> null
            }?.let {
                items.add(it)
            }
        }
        if (items.isEmpty()) {
            items.add(Loader())
        }
        data.replaceData(items)
    }

    private fun showLoader() {
        data.replaceData(arrayListOf<Any>(Loader()))
    }
}