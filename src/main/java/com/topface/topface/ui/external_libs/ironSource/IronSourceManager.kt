package com.topface.topface.ui.external_libs.ironSource

import android.app.Activity
import com.ironsource.adapters.supersonicads.SupersonicConfig
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.OfferwallListener
import com.topface.framework.utils.Debug
import com.topface.statistics.android.Slices
import com.topface.statistics.generated.IronSourceStatisticsGeneratedStatistics
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
        const val NAME = "IRONSRC"
    }

    val offerwallObservable: Observable<IronSourceOfferwallEvent>
    private var mInitSuccessSubscription: Subscription? = null
    var mEmitter: Emitter<IronSourceOfferwallEvent>? = null

    init {
        offerwallObservable = Observable.fromEmitter<IronSourceOfferwallEvent>({
            mEmitter = it
        }, Emitter.BackpressureMode.LATEST).share()
        IronSource.setOfferwallListener(object : OfferwallListener {
            override fun onOfferwallAvailable(isAvailable: Boolean) {
                Debug.log("$TAG onOfferwallAvailable = $isAvailable")
                mEmitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallAvailable(isAvailable))
            }

            override fun onOfferwallShowFailed(error: IronSourceError?) {
                Debug.log("$TAG onOfferwallShowFailed error $error")
                mEmitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallShowFailed(error))
            }

            override fun onOfferwallClosed() {
                Debug.log("$TAG onOfferwallClosed")
                mEmitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallClosed())
            }

            override fun onOfferwallAdCredited(credits: Int, totalCredits: Int, totalCreditsFlag: Boolean): Boolean {
                Debug.log("$TAG onOfferwallAdCredited $credits $totalCredits $totalCreditsFlag")
                mEmitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallAdCredited(credits, totalCredits, totalCreditsFlag))
                return false
            }

            override fun onOfferwallOpened() {
                Debug.log("$TAG onOfferwallOpened")
                mEmitter?.onNext(IronSourceOfferwallEvent.getOnOfferwallOpened())
            }

            override fun onGetOfferwallCreditsFailed(error: IronSourceError?) {
                Debug.log("$TAG onGetOfferwallCreditsFailed error $error")
                mEmitter?.onNext(IronSourceOfferwallEvent.getOnGetOfferwallCreditsFailed(error))
            }
        })
        IronSource.setLogListener { ironSourceTag, s, i -> Debug.log("$TAG IronSourceTag:$ironSourceTag $s $i") }
    }

    fun initSdk(activity: Activity) {
        IronSource.init(activity, APP_KEY, IronSource.AD_UNIT.OFFERWALL)
        IntegrationHelper.validateIntegration(activity);
    }

    fun showOfferwall(plc: String, from: String) {
        IronSourceStatisticsGeneratedStatistics.sendNow_IRON_SOURCE_SHOW_OFFERS(Slices().apply {
            putSlice("ref", plc.getIronSourceType())
            putSlice("plc", from)
        })
        SupersonicConfig.getConfigObj().offerwallCustomParams = hashMapOf(Pair("plc", from))
        if (IronSource.isOfferwallAvailable()) {
            IronSource.showOfferwall(plc)
        } else {
            mInitSuccessSubscription = offerwallObservable
                    .filter { it.type == IronSourceOfferwallEvent.OFFERWALL_AVAILABLE }
                    .first()
                    .subscribe(shortSubscription({
                    }, {
                        IronSource.showOfferwall(plc)
                    }))
        }
    }

    fun showOfferwallByType(type: String, from: String) {
        type.getIronSourcePlc()?.let {
            showOfferwall(it, from)
        }
    }

    fun emmitNewState(event: IronSourceOfferwallEvent) {
        mEmitter?.onNext(event)
    }
}