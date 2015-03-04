package com.topface.topface.requests.transport;

import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;

import java.io.IOException;

public interface IApiTransport {
    IApiResponse sendRequestAndReadResponse(IApiRequest request) throws IOException;
}
