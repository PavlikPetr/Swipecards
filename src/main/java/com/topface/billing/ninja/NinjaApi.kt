package com.topface.billing.ninja

import retrofit2.http.GET
import retrofit2.http.QueryMap
import rx.Observable

/**
 * Api methods for PaymentNinja via Retrofit
 * Created by m.bayutin on 03.03.17.
 */
internal interface Request {
    @GET(AddCardRequest.ADD_CARD_LINK)
    fun setParams(@QueryMap params: Map<String, String>): Observable<AddCardResponse>
}

