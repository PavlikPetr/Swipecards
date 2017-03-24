package com.topface.topface.utils.extensions

import android.os.Looper
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.requests.ApiResponse
import com.topface.topface.requests.DataApiHandler
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.requests.response.SimpleResponse
import rx.Emitter
import rx.Observable

/**
 * Различные методы для запросов
 * Created by ppavlik on 22.03.17.
 */

/**
 * Get subscriber on PaymentNinjaPurchaseRequest
 */
fun PaymentNinjaPurchaseRequest.getRequestSubscriber() =
        Observable.fromEmitter<SimpleResponse>({ emitter ->
            callback(object : DataApiHandler<SimpleResponse>(Looper.getMainLooper()) {
                override fun success(data: SimpleResponse?, response: IApiResponse?) = emitter.onNext(data)
                override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response.toString(), SimpleResponse::class.java)
                override fun fail(codeError: Int, response: IApiResponse) {
                    emitter.onError(Throwable(codeError.toString()))
                }

                override fun always(response: IApiResponse) {
                    super.always(response)
                    emitter.onCompleted()
                }
            }).exec()
        }, Emitter.BackpressureMode.LATEST)