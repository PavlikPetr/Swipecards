package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ApiHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Class for building request from other ones
 */
public class RequestBuilder {

    private Context mContext;
    private List<IApiRequest> mRequests = new LinkedList<>();
    private ApiHandler mApiHandler;

    public RequestBuilder(Context context) {
        mContext = context;
    }

    /**
     * Add request in first place
     *
     * @param request should be single(!) request
     * @return
     */
    @SuppressWarnings("unused")
    public RequestBuilder firstRequest(IApiRequest request) {
        mRequests.add(0, request);
        return this;
    }

    /**
     * Add request with handler in first place
     *
     * @param request should be single(!) request
     * @return
     */
    public RequestBuilder firstRequest(IApiRequest request, ApiHandler handler) {
        mRequests.add(0, request.callback(handler));
        return this;
    }

    public RequestBuilder request(IApiRequest request) {
        return request.intoBuilder(this);
    }

    public RequestBuilder request(IApiRequest request, ApiHandler handler) {
        return request.callback(handler).intoBuilder(this);
    }

    public RequestBuilder singleRequest(ApiRequest request) {
        mRequests.add(request);
        return this;
    }

    public RequestBuilder multipleRequest(MultipartApiRequest multipartRequest) {
        mRequests.addAll(multipartRequest.getRequests().values());
        mApiHandler = multipartRequest.getHandler();
        return this;
    }

    public IApiRequest build() {
        if (mRequests.size() > 1) {
            MultipartApiRequest multipartRequest = new ParallelApiRequest(mContext);
            multipartRequest.addRequests(mRequests);
            if (mApiHandler != null) {
                multipartRequest.callback(mApiHandler);
            }
            multipartRequest.setFrom(getClass().getSimpleName());
            return multipartRequest;
        } else if (mRequests.size() == 1) {
            return mRequests.get(0);
        } else {
            return null;
        }
    }
}
