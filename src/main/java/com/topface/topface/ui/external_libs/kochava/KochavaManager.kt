package com.topface.topface.ui.external_libs.kochava

import android.os.Handler
import android.util.Log
import com.kochava.android.tracker.EventParameters
import com.kochava.android.tracker.EventType
import com.kochava.android.tracker.Feature
import com.topface.framework.utils.Debug
import com.topface.topface.App
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
            msg?.data?.getString(Feature.ATTRIBUTION_DATA)?.let {
                Log.e(TAG, "catch attribution data $it")
//                Debug.log("$TAG catch attribution data $it")
                sendReferralTrack()
            }
            false
        }))
        // turn on kochava logs for debug/qa builds and editors users
        Feature.setErrorDebug(!Debug.isDebugLogsEnabled())
        Feature.enableDebug(true)
        kochavaTracker.run {
            Log.e(TAG, "init kochava. Create instance of tracker.")
//            Debug.log("$TAG init kochava. Create instance of tracker.")
        }
        // register running state manager reporter and send event about session start/end to kochava
        App.getAppComponent().runningStateManager()
                .registerAppChangeStateListener(object : RunningStateManager.OnAppChangeStateListener {
                    override fun onAppForeground(timeOnStart: Long) {
                        Log.e(TAG, "send start session event")
//                        Debug.log("$TAG send start session event")
                        kochavaTracker.startSession()
                    }

                    override fun onAppBackground(timeOnStop: Long, timeOnStart: Long) {
                        Log.e(TAG, "send end session event")
//                        Debug.log("$TAG send end session event")
                        kochavaTracker.endSession()
                    }
                })
        Log.e(TAG, "kochava device id ${Feature.getKochavaDeviceId()}")
//        Debug.log("$TAG kochava device id ${Feature.getKochavaDeviceId()}")
    }

    /**
     * Send event of purchase in app
     *
     * @param price the price of purchase
     * @param quantity the quantity of products
     * @param skuId the name of product
     */
    fun purchaseEvent(price: Float, quantity: Float, skuId: String) {
        Log.e(TAG, "send purchase event. Product : $skuId quantity : $quantity price : $price")
//        Debug.log("$TAG send purchase event. Product : $skuId quantity : $quantity price : $price")
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
        Log.e(TAG, "send registration event")
//        Debug.log("$TAG send registration event")
        kochavaTracker.eventStandard(EventParameters(EventType.RegistrationComplete))
    }

    fun sendReferralTrack() =
            if (!AuthToken.getInstance().isEmpty) {
                val attrData = Feature.getAttributionData()
                if (attrData.isNotEmpty()) {
                    Log.e(TAG, "send kochava referrerTrack request with $attrData")
//                    Debug.log("$TAG send kochava referrerTrack request with $attrData")
                    ReferrerRequest(App.getContext(), attrData).exec()
                } else {
                    Log.e(TAG, "send kochava referrerTrack request impossible, attributionData are empty")
//                    Debug.log("$TAG send kochava referrerTrack request impossible, attributionData are empty")
                }
            } else {
                Log.e(TAG, "send kochava referrerTrack request impossible, user has not yet authorized")
//                Debug.log("$TAG send kochava referrerTrack request impossible, user has not yet authorized")
            }
}