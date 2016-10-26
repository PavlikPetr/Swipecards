package com.topface.topface.statistics

import com.topface.statistics.android.Slices
import com.topface.statistics.android.StatisticsTracker
import com.topface.topface.App

/**
 * отправляет статистику о показе рекламы, что в полном объеме отображается на экране
 * Created by siberia87 on 24.10.16.
 */
class AppBannerStatistics {

    companion object {
        private val BANNER_SHOW = "mobile_app_of_the_day_show"
        private val BANNER_CLICK = "mobile_app_of_the_day_click"
        private val PLC = "plc"

        private fun send(command: String, slices: Slices?) =
                StatisticsTracker.getInstance().sendEvent(command, 1, slices)

        fun sendBannerShown(bannerId: String) = send(BANNER_SHOW, generateSlices(bannerId))

        fun sendBannerClicked(bannerId: String) = send(BANNER_CLICK, generateSlices(bannerId))

        private fun generateSlices(value: String) = Slices().putSlice(PLC, value)

    }
}