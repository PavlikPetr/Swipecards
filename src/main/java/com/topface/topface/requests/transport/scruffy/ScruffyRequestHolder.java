package com.topface.topface.requests.transport.scruffy;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MultipartApiRequest;
import com.topface.topface.requests.MultipartApiResponse;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.statistics.ScruffyStatistics;

import org.apache.http.HttpStatus;

public class ScruffyRequestHolder implements RequestHolder {
    private IApiResponse mResponse;
    private IApiRequest mRequest;
    private boolean mIsCanceled = false;

    public ScruffyRequestHolder() {
    }

    public void setRequest(IApiRequest request) {
        mRequest = request;
    }

    @Override
    public void setResponse(IApiResponse response) {
        synchronized (this) {
            Debug.log("Scruffy: setResponse()");
            mResponse = response;
            notifyAll();
            Debug.log("Scruffy: holder notifyAll()");
        }
    }

    @Override
    public void setResponse(ScruffyRequest response) {
        IApiResponse resp;
        if (mRequest instanceof MultipartApiRequest) {
            resp = new MultipartApiResponse(
                    HttpStatus.SC_OK,
                    response.getContentType(),
                    response.getBody()
            );
        } else {
            resp = new ApiResponse(response.getBody());
        }
        setResponse(resp);
        if (resp.getResultCode() < ErrorCodes.RESULT_OK) {
            ScruffyStatistics.sendScruffyResponseFail("InnerResultCode: " + resp.getResultCode());
        } else {
            ScruffyStatistics.sendScruffyResponseSuccess();
        }
    }

    @Override
    public IApiRequest getRequest() {
        return mRequest;
    }

    @Override
    public String getId() {
        return mRequest.getId();
    }

    @Override
    public IApiResponse getResponse() {
        if (mResponse != null) {
            return mResponse;
        } else {
            return new ApiResponse(ErrorCodes.NULL_RESPONSE, "Response from Scruffy is null");
        }
    }

    @Override
    public void cancel() {
        synchronized (this) {
            mIsCanceled = true;
            notifyAll();
            Debug.log("Scruffy: cancel request notifyAll()");
        }
    }

    public boolean isCompleted() {
        return mIsCanceled || mResponse != null;
    }
}
