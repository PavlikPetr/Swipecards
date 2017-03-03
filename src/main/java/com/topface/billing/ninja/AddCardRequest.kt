package com.topface.billing.ninja

import android.content.Context
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.SimpleApiHandler
import com.topface.topface.ui.external_libs.offers.OffersUtils
import rx.Emitter
import rx.Observable
import java.util.*

/**
 * Add card to payment ninja request
 * Created by m.bayutin on 02.03.17.
 */
class AddCardRequest {
    companion object {
        const val ADD_CARD_LINK = "https://api.payment.ninja/v1/card/getToken"
        const val KEY_PROJECT = "project"
        const val KEY_NUMBER = "number"
        const val KEY_EXP_MONTH = "expiration_month"
        const val KEY_EXP_YEAR = "expiration_year"
        const val KEY_SECURITY_CODE = "security_code"
    }

    fun getRequestObservable(context: Context, addCardModel: AddCardModel) =
        OffersUtils.getRequestInstance(ADD_CARD_LINK)
            .create(Request::class.java)
            .setParams(HashMap<String, String>().apply{
                put(KEY_PROJECT, addCardModel.project)
                put(KEY_NUMBER, addCardModel.number)
                put(KEY_EXP_MONTH, addCardModel.expirationMonth)
                put(KEY_EXP_YEAR, addCardModel.expirationYear)
                put(KEY_SECURITY_CODE, addCardModel.securityCode)
            })
            .flatMap { addCardResponse -> getSendCardTokenRequestObservable(context, addCardModel.email, addCardResponse) }

    private fun getSendCardTokenRequestObservable(context: Context, email: String, addCardResponse: AddCardResponse) =
        Observable.fromEmitter<IApiResponse>( { emitter ->
            val sendRequest = SendCardTokenRequest(context, SendCardTokenModel(addCardResponse.id, email))
                sendRequest.callback(object : SimpleApiHandler() {
                    override fun success(response: IApiResponse) = emitter.onNext(response)
                    override fun fail(codeError: Int, response: IApiResponse) = emitter.onError(Throwable(codeError.toString()))
                    override fun always(response: IApiResponse) {
                        super.always(response)
                        emitter.onCompleted()
                    }
                }).exec()
            }, Emitter.BackpressureMode.NONE)
}