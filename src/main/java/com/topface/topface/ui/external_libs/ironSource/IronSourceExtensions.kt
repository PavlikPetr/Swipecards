package com.topface.topface.ui.external_libs.ironSource

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
 * Get IronSource type by offers place
 *
 * @param array - list of places
 */
fun String.getIronSourceType(array: List<OfferwallPlace>) = array.find { it.name == this }?.type

/**
 * Get IronSource type by offers place. Places array from userGetAppOptions
 */
fun String.getIronSourceType() = getIronSourceType(App.get().options.offerwallWithPlaces.getPlaces())