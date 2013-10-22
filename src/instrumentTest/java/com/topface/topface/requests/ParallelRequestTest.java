package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.Debug;

public class ParallelRequestTest extends AbstractThreadTest {

    public void testParallelRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getContext();
                ParallelApiRequest request = new ParallelApiRequest(context);
                request.addRequest(new AppOptionsRequest(context).callback(new DataApiHandler<Options>() {
                    @Override
                    protected void success(Options data, IApiResponse response) {
                        Debug.log("[ParallelRequest] Options response " + response);
                        assertNotNull("Options is null", data);
                    }

                    @Override
                    protected Options parseResponse(ApiResponse response) {
                        return new Options(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Debug.error("[ParallelRequest] Options response error " + response);
                        assertTrue("Fail options request: " + codeError, false);
                        stopTest("testParallelRequest");
                    }
                }));
                request.addRequest(new TestRequest(null, null, ErrorCodes.SESSION_NOT_FOUND, context).callback(new DataApiHandler<Profile>() {

                    @Override
                    protected void success(Profile data, IApiResponse response) {
                        Debug.log("[ParallelRequest] Profile response" + response);
                        assertTrue("Profile is null", data.uid > 0);
                    }

                    @Override
                    protected Profile parseResponse(ApiResponse response) {
                        return Profile.parse(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Debug.error("[ParallelRequest] Profile response error " + response);
                        assertTrue("[ParallelRequest] Fail profile request: " + codeError, false);
                        stopTest("testParallelRequest");
                    }
                }));

                request.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        Debug.log("[ParallelRequest] Parallel response" + response);
                        stopTest("testParallelRequest");
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Debug.error("[ParallelRequest] Parallel response error " + response);
                        assertTrue("Parallel request error: " + codeError, false);
                        stopTest("testParallelRequest");
                    }
                }).exec();
            }
        }, "testParallelRequest");
    }
}
