package com.topface.topface.requests.v7;


import android.content.Context;

import com.topface.topface.requests.AbstractThreadTest;
import com.topface.topface.requests.ChangeLoginRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;

public class ChangeRequestTest extends AbstractThreadTest {

    public static final String EMAIL_TEST = "iovorobiev@gmail.com";

    public void testChangeRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getTargetContext();
                ChangeLoginRequest request = new ChangeLoginRequest(context,
                        EMAIL_TEST);
                request.callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        assertEquals(EMAIL_TEST, response.getJsonResult().optString("login"));
                        stopTest("testChangeRequest");
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {

                        assertTrue("Register error: " + codeError, false);
                        stopTest("testChangeRequest");
                    }
                }).exec();
            }
        }, "testChangeRequest");
    }
}
