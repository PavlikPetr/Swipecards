package com.topface.topface.api

import android.os.Bundle
import com.topface.topface.api.requests.BaseScruffyRequest

interface IRequestFactory<T : Any> {
    fun construct(arg: Bundle = Bundle()): BaseScruffyRequest<T>
}