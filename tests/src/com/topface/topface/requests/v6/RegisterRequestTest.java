package com.topface.topface.requests.v6;

import android.content.Context;
import com.topface.topface.data.Register;
import com.topface.topface.requests.AbstractThreadTest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.RegisterRequest;

public class RegisterRequestTest extends AbstractThreadTest {

    public void testRegisterRequest() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                Context context = getInstrumentation().getContext();
                RegisterRequest request = new RegisterRequest(context,
                        "iovorobiev@mail.ru",
                        "asdasd",
                        "ilya",
                        123456,
                        1);
                request.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        Register reg = new Register(response);
                        assertNotSame(0, reg.getUserId());
                        stopTest("testRegisterRequest");
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {

                        assertTrue("Register error: " + codeError, false);
                        stopTest("testRegisterRequest");
                    }
                }).exec();
            }
        }, "testRegisterRequest");
    }
}
