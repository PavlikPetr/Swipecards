package com.topface.topface.ui.external_libs.ironSource

import android.app.Activity
import com.topface.topface.App
import com.topface.topface.api.responses.OfferwallPlace

/**
 * Created by ppavlik on 30.05.17.
 * Набор расширений для работы с IronSource
 */

/**
 * Get IronSource plc by product type
 *
 * @param array - list of places
 */
fun String.getIronSourcePlc(array: List<OfferwallPlace>) = array.find { it.type == this }?.name

/**
 * Get IronSource plc by product type. Places array from userGetAppOptions
 */
fun String.getIronSourcePlc() = getIronSourcePlc(App.get().options.offerwallWithPlaces.getPlaces())

/**
 * Show IronSource likes offerwall in current activity
 */
fun Activity.showLikesOfferwall() = App.getAppComponent().ironSourceManager().showLikesOfferwall(this)

/**
 * Show IronSource coins offerwall in current activity
 */
fun Activity.showCoinsOfferwall() = App.getAppComponent().ironSourceManager().showCoinsOfferwall(this)

/**
 * Show IronSource vip offerwall in current activity
 */
fun Activity.showVipOfferwall() = App.getAppComponent().ironSourceManager().showVipOfferwall(this)

/**
 * Show IronSource offerwall by type
 *
 * @param type - offerwall type
 */
fun Activity.showOfferwallByType(type: String) = App.getAppComponent().ironSourceManager().showOfferwallByType(type, this)