package com.topface.topface.requests.transport.scruffy;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.transport.IApiTransport;

import java.io.IOException;


public class ScruffyApiTransport implements IApiTransport {

    public static final String TRANSPORT_NAME = "scruffy";
    public static final int REQUEST_TIMEOUT = 10000;
    private final ScruffyRequestHolder mHolder;

    public ScruffyApiTransport() {
        mHolder = new ScruffyRequestHolder();
    }

    @Override
    public IApiResponse sendRequestAndReadResponse(IApiRequest request) throws IOException {
        synchronized (mHolder) {
            mHolder.setRequest(request);

            Debug.log("Scruffy:: Before addRequest");
            long requestStart = System.currentTimeMillis();
            ScruffyRequestManager.getInstance().addRequest(mHolder);
            while (!mHolder.isCompleted() && !isTimedOut(requestStart)) {
                Debug.log("Scruffy:: before wait");
                try {
                    mHolder.wait(REQUEST_TIMEOUT + 1000);
                    Debug.log("Scruffy:: after wait");
                } catch (InterruptedException e) {
                    Debug.error("Scruffy:: timeout ", e);
                    mHolder.setResponse(
                            new ApiResponse(ErrorCodes.ERRORS_PROCESSED, "Thread exception " + e.getMessage())
                    );
                }
            }
            if (!mHolder.isCompleted() && isTimedOut(requestStart)) {
                mHolder.setResponse(new ApiResponse(ErrorCodes.CONNECTION_ERROR, "Request timeout"));
                ScruffyRequestManager.getInstance().reconnect();
            }
            Debug.log("Scruffy:: response " + mHolder.getResponse());

            return mHolder.getResponse();
        }
    }

    private boolean isTimedOut(long requestStart) {
        return (System.currentTimeMillis() - requestStart) > REQUEST_TIMEOUT;
    }

}
