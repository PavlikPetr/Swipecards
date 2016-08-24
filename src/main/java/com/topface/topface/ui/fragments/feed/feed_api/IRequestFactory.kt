package com.topface.topface.ui.fragments.feed.feed_api

import android.os.Bundle
import com.topface.topface.requests.ApiRequest

/**
 * Интерфес фабрики запросов
 * Created by tiberal on 08.08.16.
 */
interface IRequestFactory {
    fun construct(arg: Bundle? = null): ApiRequest?
}