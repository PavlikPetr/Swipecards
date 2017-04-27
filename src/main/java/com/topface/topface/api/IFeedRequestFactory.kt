package com.topface.topface.api

import android.os.Bundle
import com.topface.topface.api.requests.FeedRequest
import com.topface.topface.api.responses.IBaseFeedResponse

interface IFeedRequestFactory {
    fun <P : IBaseFeedResponse> construct(arg: Bundle, responseClass: Class<P>): FeedRequest<P>
}