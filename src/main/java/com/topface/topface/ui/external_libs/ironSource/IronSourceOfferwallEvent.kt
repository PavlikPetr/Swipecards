package com.topface.topface.ui.external_libs.ironSource

import android.os.Bundle
import android.support.annotation.IntDef
import com.ironsource.mediationsdk.logger.IronSourceError

/**
 * Created by ppavlik on 30.05.17.
 * Объект события IronSource Offerwall
 */
data class IronSourceOfferwallEvent(@OfferwallState var type: Long = IronSourceOfferwallEvent.UNDEFINED, var extra: Bundle = Bundle()) {
    companion object {
        @IntDef(UNDEFINED, OFFERWALL_AVAILABLE, OFFERWALL_SHOW_FAILED, OFFERWALL_CLOSED,
                OFFERWALL_AD_CREDITED, OFFERWALL_OPENED, GET_OFFERWALL_CREDITS_FAILED, CALL_OFFERWALL)
        @Retention(AnnotationRetention.SOURCE)
        annotation class OfferwallState

        const val UNDEFINED = 0L
        const val OFFERWALL_AVAILABLE = 1L
        const val OFFERWALL_SHOW_FAILED = 2L
        const val OFFERWALL_CLOSED = 3L
        const val OFFERWALL_AD_CREDITED = 4L
        const val OFFERWALL_OPENED = 5L
        const val GET_OFFERWALL_CREDITS_FAILED = 6L
        const val CALL_OFFERWALL = 7L

        const val IS_AVAILABLE = "IronSourceOfferwallEvent.Extra.IsAvailable"
        const val ERROR_CODE = "IronSourceOfferwallEvent.Extra.ErrorCode"
        const val ERROR_MESSAGE = "IronSourceOfferwallEvent.Extra.ErrorMessage"
        const val CREDITS = "IronSourceOfferwallEvent.Extra.Credits"
        const val TOTAL_CREDITS = "IronSourceOfferwallEvent.Extra.TotalCredits"
        const val TOTAL_CREDITS_FLAG = "IronSourceOfferwallEvent.Extra.TotalCreditsFlag"

        fun getOnOfferwallAvailable(state: Boolean) =
                IronSourceOfferwallEvent(OFFERWALL_AVAILABLE, Bundle().apply { putBoolean(IS_AVAILABLE, state) })

        fun getOnOfferwallShowFailed(error: IronSourceError?) =
                IronSourceOfferwallEvent(OFFERWALL_SHOW_FAILED, Bundle().apply {
                    error?.let {
                        putInt(ERROR_CODE, it.errorCode)
                        putString(ERROR_MESSAGE, it.errorMessage)
                    }
                })

        fun getOnOfferwallClosed() =
                IronSourceOfferwallEvent(OFFERWALL_CLOSED)

        fun getOnOfferwallAdCredited(credits: Int, totalCredits: Int, totalCreditsFlag: Boolean) =
                IronSourceOfferwallEvent(OFFERWALL_AD_CREDITED, Bundle().apply {
                    putInt(CREDITS, credits)
                    putInt(TOTAL_CREDITS, totalCredits)
                    putBoolean(TOTAL_CREDITS_FLAG, totalCreditsFlag)
                })

        fun getOnOfferwallOpened() =
                IronSourceOfferwallEvent(OFFERWALL_OPENED)

        fun getOnGetOfferwallCreditsFailed(error: IronSourceError?) =
                IronSourceOfferwallEvent(GET_OFFERWALL_CREDITS_FAILED, Bundle().apply {
                    error?.let {
                        putInt(ERROR_CODE, it.errorCode)
                        putString(ERROR_MESSAGE, it.errorMessage)
                    }
                })

        fun getOnOfferwallCall() =
                IronSourceOfferwallEvent(CALL_OFFERWALL)
    }
}