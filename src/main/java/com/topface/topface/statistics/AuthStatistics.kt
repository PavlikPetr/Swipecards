package com.topface.topface.statistics

import com.topface.statistics.android.Slices
import com.topface.statistics.android.StatisticsTracker

/**
 * Тут статистика авторизации
 * Created by siberia87 on 11.11.16.
 */
class AuthStatistics {
    companion object {
        private val APP_FIRST_START_KEY = "mobile_app_first_start"
        private val FIRST_AUTH_KEY = "mobile_first_auth"
        private val DEVICE_ACTIVATED_KEY = "mobile_device_activated"
        private val PLT_SLICE = "plc"
        private val VAL_SLICE = "val"

        private fun send(command: String, slices: Slices?) =
                StatisticsTracker.getInstance().sendEvent(command, 1, slices)

        fun sendFirstStartApp() {
            send(APP_FIRST_START_KEY, null)
        }

        fun sendFirstAuth(platform: String, authStatus: String) {
            val slice = Slices()
            slice.putSlice(AuthStatistics.PLT_SLICE, platform)
            slice.putSlice(AuthStatistics.VAL_SLICE, authStatus)
            send(FIRST_AUTH_KEY, slice)
        }

        fun sendDeviceActivated() {
            send(DEVICE_ACTIVATED_KEY, null)
        }

    }
}