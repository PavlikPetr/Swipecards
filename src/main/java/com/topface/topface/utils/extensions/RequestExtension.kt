package com.topface.topface.utils.extensions

import android.os.Looper
import com.topface.framework.JsonUtils
import com.topface.topface.requests.ApiResponse
import com.topface.topface.requests.DataApiHandler
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.PaymentNinjaPurchaseRequest
import com.topface.topface.requests.response.SimpleResponse
import com.topface.topface.utils.Utils
import rx.Emitter
import rx.Observable

/**
 * Различные методы для запросов
 * Created by ppavlik on 22.03.17.
 */

/**
 * Get subscriber on PaymentNinjaPurchaseRequest
 */
fun PaymentNinjaPurchaseRequest.getRequestSubscriber(): Observable<SimpleResponse> =
        Observable.fromEmitter<SimpleResponse>({ emitter ->
            callback(object : DataApiHandler<SimpleResponse>(Looper.getMainLooper()) {
                override fun success(data: SimpleResponse?, response: IApiResponse?) = emitter.onNext(data)
                override fun parseResponse(response: ApiResponse?) = JsonUtils.fromJson(response?.jsonResult?.toString() ?: Utils.EMPTY, SimpleResponse::class.java)
                override fun fail(codeError: Int, response: IApiResponse) {
                    emitter.onError(Throwable(response.jsonResult.apply { put("errorCode", codeError) }.toString()))
                }

                override fun always(response: IApiResponse) {
                    super.always(response)
                    emitter.onCompleted()
                }
            }).exec()
        }, Emitter.BackpressureMode.LATEST)