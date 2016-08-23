package com.topface.topface.requests.transport.scruffy

import android.content.Context
import com.topface.scruffy.IUserAgentProvider
import com.topface.scruffy.ScruffyManager
import com.topface.scruffy.ScruffyRequest
import com.topface.topface.App
import com.topface.topface.utils.http.HttpUtils

/**
 * Created by ppavlik on 23.08.16.
 *
 */
object ScruffyManager {

    private var mInstance: ScruffyManager? = null

    fun getScruffy(context: Context): ScruffyManager? {
        if (mInstance == null) {
            mInstance = ScruffyManager(context, arrayOf(App.getAppConfig().getScruffyApiUrl()), object : IUserAgentProvider {
                override fun createUserAgent() = HttpUtils.getUserAgent("Scruffy/2")
            })
        }
        return mInstance
    }

    fun <T> sendRequest(context: Context, request: ScruffyRequest<T>) {
        getScruffy(context)?.sendRequest(request);
    }

}