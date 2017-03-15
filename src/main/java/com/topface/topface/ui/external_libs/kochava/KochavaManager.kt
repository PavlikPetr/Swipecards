package com.topface.topface.ui.external_libs.kochava

import android.os.Handler
import com.kochava.android.tracker.EventParameters
import com.kochava.android.tracker.EventType
import com.kochava.android.tracker.Feature
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.requests.ReferrerLogRequest
import com.topface.topface.requests.ReferrerRequest
import com.topface.topface.utils.RunningStateManager
import com.topface.topface.utils.social.AuthToken

/**
 * Kochava manager for initialization lib and send some events
 * Created by ppavlik on 28.02.17.
 */

class KochavaManager {

    companion object {
        private const val APP_GUID = "kotopface-android-s07"
        private const val TAG = "KochavaManager"
    }

    val kochavaTracker by lazy {
        Feature(App.getContext(), hashMapOf(Pair<String, Any>(Feature.INPUTITEMS.KOCHAVA_APP_ID, APP_GUID),
                Pair<String, Any>(Feature.INPUTITEMS.REQUEST_ATTRIBUTION, true)))
    }

    /**
     * The Constructor should be called somewhere within the start-up logic of your application
     * (such as the onCreate method of your application class or in a Singleton created from
     * the Application class)
     */
    fun initTracker() {
        // to receive attribution data via a callback
        // we need register callback before tracker init
        Feature.setAttributionHandler(Handler(Handler.Callback { msg ->
            // check attributionData to minimize of kochava instance using
            ReferrerLogRequest(App.getContext(), kochavaData = msg?.data?.getString(Feature.ATTRIBUTION_DATA)).exec()
            msg?.data?.getString(Feature.ATTRIBUTION_DATA)?.let {
                sendReferralTrack()
            }
            false
        }))
        kochavaTracker.run {
            Debug.log("$TAG init kochava. Create instance of tracker.")
        }
        // turn on kochava logs for debug/qa builds and editors users
        Feature.setErrorDebug(!Debug.isDebugLogsEnabled())
        // register running state manager reporter and send event about session start/end to kochava
        App.getAppComponent().runningStateManager()
                .registerAppChangeStateListener(object : RunningStateManager.OnAppChangeStateListener {
                    override fun onAppForeground(timeOnStart: Long) {
                        Debug.log("$TAG send start session event")
                        kochavaTracker.startSession()
                    }

                    override fun onAppBackground(timeOnStop: Long, timeOnStart: Long) {
                        Debug.log("$TAG send end session event")
                        kochavaTracker.endSession()
                    }
                })
        Debug.log("$TAG kochava device id ${Feature.getKochavaDeviceId()}")
    }

    /**
     * Send event of purchase in app
     *
     * @param price the price of purchase
     * @param quantity the quantity of products
     * @param skuId the name of product
     */
    fun purchaseEvent(price: Float, quantity: Float, skuId: String) {
        Debug.log("$TAG send purchase event. Product : $skuId quantity : $quantity price : $price")
        kochavaTracker.eventStandard(EventParameters(EventType.Purchase).apply {
            name(skuId)
            quantity(quantity)
            price(price)
        })
    }

    /**
     * Send event registration
     */
    fun registration() {
        Debug.log("$TAG send registration event")
        kochavaTracker.eventStandard(EventParameters(EventType.RegistrationComplete))
    }

    fun sendReferralTrack() =
            if (!AuthToken.getInstance().isEmpty) {
                val attrData = Feature.getAttributionData()
                if (attrData.isNotEmpty()) {
                    Debug.log("$TAG send kochava referrerTrack request with $attrData")
                    ReferrerRequest(App.getContext(), attrData).exec()
                } else {
                    Debug.log("$TAG send kochava referrerTrack request impossible, attributionData are empty")
                }
            } else {
                Debug.log("$TAG send kochava referrerTrack request impossible, user has not yet authorized")
            }
}