package com.topface.topface.statistics

import com.topface.statistics.android.Slices
import com.topface.statistics.android.StatisticsTracker

/**
 * Тут статистика авторизации
 * Created by siberia87 on 11.11.16.
 */
class AuthStatistics {
    companion object {
        private const val APP_FIRST_START_KEY = "mobile_app_first_start"
        private const val FIRST_AUTH_KEY = "mobile_first_auth"
        private const val DEVICE_ACTIVATED_KEY = "mobile_device_activated"
        private const val PLT_SLICE = "plt"
        private const val VAL_SLICE = "val"
        const val DEFAULT_AUTH_STATUS = "created"

        private fun send(command: String, slices: Slices?) =
                StatisticsTracker.getInstance().sendEvent(command, 1, slices)

        @JvmStatic fun sendFirstStartApp() = send(APP_FIRST_START_KEY, null)

        @JvmStatic fun sendFirstAuth(platform: String, authStatus: String) = send(FIRST_AUTH_KEY,
                with(Slices()) {
                    putSlice(AuthStatistics.PLT_SLICE, platform)
                    putSlice(AuthStatistics.VAL_SLICE, authStatus)
                })

        fun sendDeviceActivated() = send(DEVICE_ACTIVATED_KEY, null)
    }
}