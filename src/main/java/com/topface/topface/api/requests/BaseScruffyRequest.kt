package com.topface.topface.api.requests

import com.topface.scruffy.ScruffyRequest
import com.topface.topface.App
import com.topface.topface.Ssid
import com.topface.topface.utils.rx.applySchedulers
import rx.Observable
import java.util.concurrent.TimeUnit

abstract class BaseScruffyRequest<T : Any> : ScruffyRequest<T>(App.getAppComponent().scruffyManager(), App.getAppComponent().scruffyManager().mEventManager) {

    override fun getSsid(): String = Ssid.get()

    override fun subscribe(): Observable<T> {
        return super.subscribe().first().timeout(20, TimeUnit.SECONDS).applySchedulers()
    }
}