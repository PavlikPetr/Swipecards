package com.topface.topface.ui.external_libs.ironSource

import android.app.Activity
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.OfferwallListener
import com.topface.framework.utils.Debug
import com.topface.topface.utils.rx.shortSubscription
import rx.Emitter
import rx.Observable
import rx.Subscription

/**
 * Created by ppavlik on 30.05.17.
 * Менеджер для работы с sdk IronSource
 */
class IronSourceManager {

    companion object {
        const val SYMPATHIES_OFFERWALL = "sympathy"
        const val COINS_OFFERWALL = "coins"
        const val VIP_OFFERWALL = "vip"

        private const val APP_KEY = "2cf0ad4d"
        private const val TAG = "IronSource"
    }

    val offerwallObservable: Observable<IronSourceOfferwallEvent>
    private var mInitSuccessSubscription: Subscription? = null

    init {
        var emitter: Emitter<IronSourceOfferwallEvent>? = null
        offerwallObservable = Observable.fromEmitter<IronSourceOfferwallEvent>({
            emitter = it
        }, Emitter.BackpressureMode.LATEST).share()
        IronSource.setOfferwallListener(object : OfferwallListener {
            override fun onOfferwallAvailable(isAvailable: Boolean) {
                Debug.log("$TAG onOfferwallAvailable = $isAvailable")
                emitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallAvailable(isAvailable))
            }

            override fun onOfferwallShowFailed(error: IronSourceError?) {
                Debug.log("$TAG onOfferwallShowFailed error $error")
                emitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallShowFailed(error))
            }

            override fun onOfferwallClosed() {
                Debug.log("$TAG onOfferwallClosed")
                emitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallClosed())
            }

            override fun onOfferwallAdCredited(credits: Int, totalCredits: Int, totalCreditsFlag: Boolean): Boolean {
                Debug.log("$TAG onOfferwallAdCredited $credits $totalCredits $totalCreditsFlag")
                emitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallAdCredited(credits, totalCredits, totalCreditsFlag))
                return false
            }

            override fun onOfferwallOpened() {
                Debug.log("$TAG onOfferwallOpened")
                emitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallOpened())
            }

            override fun onGetOfferwallCreditsFailed(error: IronSourceError?) {
                Debug.log("$TAG onGetOfferwallCreditsFailed error $error")
                emitter?.onNext(IronSourceOfferwallEvent.getOnGetOfferwallCreditsFailed(error))
            }
        })
        IronSource.setLogListener { ironSourceTag, s, i -> Debug.log("$TAG IronSourceTag:$ironSourceTag $s $i") }
    }

    fun initSdk(activity: Activity) {
        IronSource.init(activity, APP_KEY, IronSource.AD_UNIT.OFFERWALL)
    }

    fun showOfferwall(plc: String, activity: Activity) {
        if (IronSource.isOfferwallAvailable()) {
            IronSource.showOfferwall(plc)
        } else {
            mInitSuccessSubscription = offerwallObservable
                    .filter { it.type == IronSourceOfferwallEvent.OFFERWALL_AVAILABLE }
                    .first()
                    .subscribe(shortSubscription { IronSource.showOfferwall(plc) })
            initSdk(activity)
        }
    }

    fun showOfferwallByType(type: String, activity: Activity) {
        type.getIronSourcePlc()?.let {
            showOfferwall(it, activity)
        }
    }

    fun showLikesOfferwall(activity: Activity) {
        showOfferwallByType(SYMPATHIES_OFFERWALL, activity)
    }

    fun showCoinsOfferwall(activity: Activity) {
        showOfferwallByType(COINS_OFFERWALL, activity)
    }

    fun showVipOfferwall(activity: Activity) {
        showOfferwallByType(VIP_OFFERWALL, activity)
    }
}