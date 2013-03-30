package com.topface.topface.requests.v6;

import android.content.Context;
import com.topface.topface.Ssid;
import com.topface.topface.requests.AbstractThreadTest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.ConfirmRequest;
import com.topface.topface.requests.handlers.ApiHandler;

public class ConfirmRequestTest extends AbstractThreadTest {

    public void testConfirmRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                final Context context = getInstrumentation().getContext();
                AuthRequest auth = new AuthRequest("iovorobiev@mail.ru", "asdasd", context);
                auth.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        Ssid.save(response.jsonResult.optString("ssid"));
                        ConfirmRequest request = new ConfirmRequest(context,
                                "iovorobiev@mail.ru",
                                "47829419-86866c0c937752a4a0d5c03ca7a49429-1359812235"
                        );
                        request.callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) {
                                assertEquals(true, response.jsonResult.optBoolean("completed"));
                                stopTest("testConfirmRequest");
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) {

                                assertTrue("Register error: " + codeError, false);
                                stopTest("testConfirmRequest");
                            }
                        }).exec();
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        stopTest("testConfirmRequest");
                    }
                }).exec();

            }
        }, "testConfirmRequest");
    }
}
