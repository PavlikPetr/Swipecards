package com.topface.topface.requests.v6;


import android.content.Context;
import com.topface.topface.data.Register;
import com.topface.topface.requests.*;

public class ChangeRequestTest extends AbstractThreadTest {

    public static final String EMAIL_TEST = "iovorobiev@gmail.com";

    public void testChangeRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getContext();
                ChangeLoginRequest request = new ChangeLoginRequest(context,
                        EMAIL_TEST);
                request.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        assertEquals(EMAIL_TEST, response.jsonResult.optString("login"));
                        stopTest("testChangeRequest");
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {

                        assertTrue("Register error: " + codeError, false);
                        stopTest("testChangeRequest");
                    }
                }).exec();
            }
        }, "testChangeRequest");
    }
}
