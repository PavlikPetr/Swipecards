package com.topface.topface.utils.http;

import com.topface.topface.requests.ApiRequest;

public interface IRequestClient {
    void registerRequest(ApiRequest request);

    void cancelRequest(ApiRequest request);
}
