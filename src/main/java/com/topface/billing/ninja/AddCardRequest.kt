package com.topface.billing.ninja

import android.content.Context
import com.topface.topface.requests.ApiRequest
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.SimpleApiHandler
import com.topface.topface.ui.external_libs.offers.OffersUtils
import org.json.JSONObject
import retrofit2.http.GET
import retrofit2.http.QueryMap
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

    fun getRequestObservable(context: Context, addCardModel: AddCardModel): Observable<IApiResponse> {
        val params = HashMap<String, String>()
        params.put(KEY_PROJECT, addCardModel.project)
        params.put(KEY_NUMBER, addCardModel.number)
        params.put(KEY_EXP_MONTH, addCardModel.expirationMonth)
        params.put(KEY_EXP_YEAR, addCardModel.expirationYear)
        params.put(KEY_SECURITY_CODE, addCardModel.securityCode)
        return OffersUtils
                .getRequestInstance(ADD_CARD_LINK)
                .create(Request::class.java)
                .setParams(params)
                .flatMap { addCardResponse -> getSendCardTokenRequestObservable(context, addCardModel.email, addCardResponse) }
    }

    private fun getSendCardTokenRequestObservable(context: Context, email: String, addCardResponse: AddCardResponse): Observable<IApiResponse> {
        return Observable.create {
            val sendRequest = SendCardTokenRequest(context, SendCardTokenModel(addCardResponse.id, email))
            sendRequest.callback(object : SimpleApiHandler() {
                override fun success(response: IApiResponse) = it.onNext(response)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            }).exec()
        }
    }

    private interface Request {
        @GET(ADD_CARD_LINK)
        fun setParams(@QueryMap params: Map<String, String>): Observable<AddCardResponse>
    }

    private class SendCardTokenRequest(context: Context, val data: SendCardTokenModel) : ApiRequest(context) {
        override fun getServiceName() = SERVICE_NAME

        override fun getRequestData() = JSONObject().apply {
            put(KEY_EMAIL, data.email)
            put(KEY_TOKEN, data.token)
        }

        companion object {
            const val SERVICE_NAME = "paymentNinja.addCard"
            const val KEY_TOKEN = "token"
            const val KEY_EMAIL = "email"
        }
    }
}