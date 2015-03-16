package com.topface.topface.requests.transport.scruffy;

import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;

public interface RequestHolder {
    void setResponse(IApiResponse response);

    void setResponse(ScruffyRequest response);

    IApiRequest getRequest();

    String getId();

    IApiResponse getResponse();

    void cancel();
}
